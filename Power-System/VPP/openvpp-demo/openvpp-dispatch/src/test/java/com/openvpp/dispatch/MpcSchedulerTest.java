package com.openvpp.dispatch;

import com.openvpp.dispatch.mpc.ChanceConstraint;
import com.openvpp.dispatch.mpc.DeviationTrigger;
import com.openvpp.dispatch.mpc.DispatchPlan;
import com.openvpp.dispatch.mpc.ErrorCorrector;
import com.openvpp.dispatch.mpc.ForecastSeries;
import com.openvpp.dispatch.mpc.Forecaster;
import com.openvpp.dispatch.mpc.GrayModelCorrector;
import com.openvpp.dispatch.mpc.MpcScheduler;
import com.openvpp.dispatch.mpc.ResourcePool;
import com.openvpp.dispatch.mpc.RollingOptimizer;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 第 33 篇 MPC 三段式骨架单测：反馈校正 / 滚动优化只执行首时段 /
 * 市场边界封顶 / 资源池限功率 / 机会约束参数化 / 偏差触发器。
 */
class MpcSchedulerTest {

    /** 测试桩：固定序列预测器 */
    private static Forecaster fixedForecaster(double... values) {
        return (now, periods) -> new ForecastSeries(values);
    }

    /** 测试桩：计划 = min(预测, 市场边界, 资源池可调)，逐时段取小 */
    private static final RollingOptimizer MIN_OPTIMIZER = (pool, series, boundary) -> {
        double[] schedule = new double[series.size()];
        for (int i = 0; i < series.size(); i++) {
            schedule[i] = Math.min(Math.min(series.valueAt(i), boundary), pool.availableKw(i));
        }
        return new DispatchPlan(schedule);
    };

    private static ResourcePool constantPool(double kw) {
        return periodIndex -> kw;
    }

    @Test
    void 反馈校正用实测误差平移预测() {
        // 上一轮预测系统性偏低 100kW（实测 8100 vs 预测 8000），偏置 +100
        GrayModelCorrector corrector = new GrayModelCorrector(4);
        double bias = corrector.estimateBias(
                new double[]{8000, 8000, 8000, 8000},
                new double[]{8100, 8100, 8100, 8100});
        assertEquals(100.0, bias, 0.01, "常值误差应被完整外推为偏置");
    }

    @Test
    void 灰色模型小样本窗口只取最近误差() {
        GrayModelCorrector corrector = new GrayModelCorrector(2);
        // 窗口 2：只取最近 2 个时段误差（30, 30），早期 10 不参与
        double bias = corrector.estimateBias(
                new double[]{0, 0, 0, 0},
                new double[]{10, 10, 30, 30});
        assertEquals(30.0, bias, 0.01, "小样本窗口应只外推最近误差");
    }

    @Test
    void 滚动优化只执行第一时段() {
        MpcScheduler scheduler = new MpcScheduler(
                fixedForecaster(8000, 8100, 8200, 8300),
                MIN_OPTIMIZER,
                new GrayModelCorrector(4),
                4);
        MpcScheduler.DispatchContext ctx = new MpcScheduler.DispatchContext(
                Instant.parse("2026-06-13T06:00:00Z"),
                new double[]{8000, 8000, 8000, 8000},
                new double[]{8100, 8100, 8100, 8100},
                9000);
        DispatchPlan plan = scheduler.cycle(constantPool(10000), ctx);
        assertEquals(1, plan.periods(), "滚动优化求解 N 时段但只执行第 1 时段");
        // 校正后序列 [8100,8200,8300,8400]，首时段 8100
        assertEquals(8100.0, plan.valueAt(0), 0.01);
    }

    @Test
    void 计划不得突破市场申报边界() {
        MpcScheduler scheduler = new MpcScheduler(
                fixedForecaster(8000, 8100, 8200, 8300),
                MIN_OPTIMIZER,
                new GrayModelCorrector(4),
                4);
        // 无误差（空序列偏置为 0），边界 8150：第三、四时段被截顶
        MpcScheduler.DispatchContext ctx = new MpcScheduler.DispatchContext(
                Instant.parse("2026-06-13T06:00:00Z"),
                new double[0], new double[0], 8150);
        DispatchPlan full = MIN_OPTIMIZER.optimize(
                constantPool(10000),
                new ForecastSeries(new double[]{8000, 8100, 8200, 8300}),
                ctx.marketBoundary());
        assertEquals(8150.0, full.valueAt(2), 0.01, "超边界时段必须被截顶");
        assertEquals(8150.0, full.valueAt(3), 0.01);
        // 调度器整体流程在边界内执行首时段
        assertEquals(8000.0, scheduler.cycle(constantPool(10000), ctx).valueAt(0), 0.01);
    }

    @Test
    void 资源池能力不足时按计划限功率() {
        DispatchPlan plan = MIN_OPTIMIZER.optimize(
                constantPool(8050),
                new ForecastSeries(new double[]{8100, 8200}),
                9000);
        assertEquals(8050.0, plan.valueAt(0), 0.01, "可调容量不足时计划取资源池上限");
    }

    @Test
    void 机会约束置信度折算可信容量() {
        // 第 14 篇口径：可信容量 = 可调容量 × 置信度
        assertEquals(950.0, ChanceConstraint.of("soc >= socMin", 95.0).credibleKw(1000), 0.01);
        assertEquals(900.0, ChanceConstraint.of("soc >= socMin", 90.0).credibleKw(1000), 0.01);
        assertEquals(0.05, ChanceConstraint.of("soc >= socMin", 95.0).violationProbability(), 1e-9);
    }

    @Test
    void 机会约束非法置信度拒绝() {
        assertThrows(IllegalArgumentException.class,
                () -> ChanceConstraint.of("soc >= socMin", 30.0), "置信度过低失去机会约束意义");
        assertThrows(IllegalArgumentException.class,
                () -> ChanceConstraint.of("soc >= socMin", 100.0), "100% 是硬约束不是机会约束");
        assertThrows(IllegalArgumentException.class,
                () -> ChanceConstraint.of(" ", 95.0), "表达式不能为空");
    }

    @Test
    void 偏差触发器按免罚带反推阈值() {
        // 第 17 篇免罚档 ≥90%：申报 5MW 时免罚带 ±500kW，阈值取 500kW
        DeviationTrigger trigger = new DeviationTrigger(500);
        assertFalse(trigger.shouldTrigger(5300, 5000), "免罚带内偏差不应触发重优化");
        assertFalse(trigger.shouldTrigger(5500, 5000), "恰好等于阈值不触发（严格大于）");
        assertTrue(trigger.shouldTrigger(5600, 5000), "超免罚带必须触发");
        assertTrue(trigger.shouldTrigger(4400, 5000), "负偏差对称触发");
    }

    @Test
    void 偏差触发器参数校验() {
        assertThrows(IllegalArgumentException.class, () -> new DeviationTrigger(-1));
    }

    @Test
    void 预测序列不可变且平移生效() {
        ForecastSeries series = new ForecastSeries(new double[]{100, 200});
        ForecastSeries shifted = series.shift(15);
        assertEquals(115.0, shifted.valueAt(0), 0.01);
        assertEquals(100.0, series.valueAt(0), 0.01, "原序列不可被平移修改");
        assertThrows(IllegalArgumentException.class, () -> new ForecastSeries(new double[0]));
    }
}
