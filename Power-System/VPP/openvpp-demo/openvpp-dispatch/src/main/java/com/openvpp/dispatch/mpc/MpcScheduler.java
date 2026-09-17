package com.openvpp.dispatch.mpc;

import java.time.Instant;

/**
 * 最小 MPC 调度器 —— 专栏第 33 篇「预测器 + 滚动优化 + 反馈校正」三段式骨架。
 *
 * 刻意保持简单：生产版的求解器可以换，三段结构不变。
 *
 * 每个调度周期执行一次：
 * <ol>
 *   <li>反馈校正：用上一轮实测误差修正预测偏置（灰色模型小样本外推）</li>
 *   <li>预测：未来 horizon 个时段，整体平移偏置</li>
 *   <li>滚动优化：求解 N 时段最优，只取第 1 时段执行</li>
 * </ol>
 *
 * 教学简化：预测器/优化器/校正器均为接口注入，本类只编排三段式流程，
 * 不内嵌具体预测算法与求解器。
 */
public class MpcScheduler {

    private final Forecaster forecaster;
    private final RollingOptimizer optimizer;
    private final ErrorCorrector corrector;
    private final int horizonPeriods;

    /**
     * @param forecaster     预测器（时序 + 卡尔曼混合的生产实现）
     * @param optimizer      滚动时域优化器
     * @param corrector      反馈校正器（灰色模型误差外推）
     * @param horizonPeriods 滚动时窗时段数
     */
    public MpcScheduler(Forecaster forecaster, RollingOptimizer optimizer,
                        ErrorCorrector corrector, int horizonPeriods) {
        this.forecaster = forecaster;
        this.optimizer = optimizer;
        this.corrector = corrector;
        this.horizonPeriods = horizonPeriods;
    }

    /**
     * 每个调度周期执行一次三段式滚动。
     *
     * @param pool 可调资源池
     * @param ctx  调度上下文（上一轮预测/实测、当前时刻、市场边界）
     * @return 只含第 1 时段的执行计划
     */
    public DispatchPlan cycle(ResourcePool pool, DispatchContext ctx) {
        // 1. 反馈校正：用上一轮实测误差修正预测偏置
        double bias = corrector.estimateBias(ctx.lastForecast(), ctx.lastActual());
        // 2. 预测：未来 horizon 个时段（校正后）
        ForecastSeries series = forecaster.forecast(ctx.now(), horizonPeriods).shift(bias);
        // 3. 滚动优化：求解 N 时段最优，只取第 1 时段执行
        DispatchPlan plan = optimizer.optimize(pool, series, ctx.marketBoundary());
        return plan.firstPeriodOnly();
    }

    /**
     * 调度上下文 —— 一轮 MPC 所需的全部输入。
     */
    public static final class DispatchContext {
        private final Instant now;
        private final double[] lastForecast;
        private final double[] lastActual;
        private final double marketBoundary;

        /**
         * @param now            当前时刻
         * @param lastForecast   上一轮预测序列（kW）
         * @param lastActual     上一轮实测序列（kW）
         * @param marketBoundary 市场申报边界（kW），计划不得随意突破
         */
        public DispatchContext(Instant now, double[] lastForecast, double[] lastActual,
                               double marketBoundary) {
            this.now = now;
            this.lastForecast = lastForecast;
            this.lastActual = lastActual;
            this.marketBoundary = marketBoundary;
        }

        public Instant now() { return now; }
        public double[] lastForecast() { return lastForecast; }
        public double[] lastActual() { return lastActual; }
        public double marketBoundary() { return marketBoundary; }
    }
}
