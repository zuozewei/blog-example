package com.openvpp.settlement;

import com.openvpp.settlement.regional.BaselineHolidayAdjuster;
import com.openvpp.settlement.regional.HuabeiPeakSettlement;
import com.openvpp.settlement.regional.JiangsuFreqRanking;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 第 34 篇区域结算规则单测：华北双重 min 直译（含悬崖条款与除零保护）、
 * 江苏排序键（报量不报价代入最高价 / 口径不含 K3）、基线天气调整。
 * 数值全部锚定文章算例，可复算。
 */
class RegionalSettlementTest {

    // 华北算例：K=1.5（火电负荷率倒数）、tC=0.25h、CC=0.4 元/kWh
    private static final double K = 1.5;
    private static final double CC = 0.4;

    @Test
    void 华北响应不足双重min叠加打折() {
        // P=900 < Pz=1000：性能系数 0.9 × 电量基数 900 × 0.25h × 0.4 × 1.5
        double amount = HuabeiPeakSettlement.settle(900, 1000, K, CC);
        assertEquals(1.5 * 0.9 * 900 * 0.25 * 0.4, amount, 0.01);
        assertEquals(121.5, amount, 0.01, "响应不足被双重 min 叠加惩罚");
    }

    @Test
    void 华北精确踩到中标容量收益最大() {
        // P = Pz = 1000：性能系数 1、电量基数 1000
        double amount = HuabeiPeakSettlement.settle(1000, 1000, K, CC);
        assertEquals(150.0, amount, 0.01, "最优策略是精确踩到中标容量");
    }

    @Test
    void 华北超额响应零奖励() {
        // P=1200 > Pz=1000（偏差 20% 未触悬崖）：性能系数封顶 1、基数封顶 1000
        double amount = HuabeiPeakSettlement.settle(1200, 1000, K, CC);
        assertEquals(150.0, amount, 0.01, "超额响应多调白送");
    }

    @Test
    void 华北悬崖条款偏差超30全时段归零() {
        assertEquals(0.0, HuabeiPeakSettlement.settle(650, 1000, K, CC),
                "偏差 35% 超悬崖，全时段不结算");
        // 恰好 30% 不触发悬崖（规则为「超过 30%」）
        double atBoundary = HuabeiPeakSettlement.settle(700, 1000, K, CC);
        assertEquals(1.5 * 0.7 * 700 * 0.25 * 0.4, atBoundary, 0.01, "偏差 30% 仍按公式结算");
        assertEquals(73.5, atBoundary, 0.01);
    }

    @Test
    void 华北中标容量为零短路返回() {
        assertEquals(0.0, HuabeiPeakSettlement.settle(100, 0, K, CC),
                "Pz 为 0 须除零保护短路返回");
    }

    @Test
    void 江苏报量不报价者排序键代入最高价() {
        // 储能 VPP 不报价：分母代入 PM=8 元/MW
        double vppKey = JiangsuFreqRanking.rankingKey(5.0, 0.98, 10.0, null, 8.0);
        assertEquals(4.9 / 8.0, vppKey, 1e-9);
        // 火电报低价 0.5 元/MW：同口径排序键反而更大
        double thermalKey = JiangsuFreqRanking.rankingKey(1.0, 0.85, 0.5, 0.5, 8.0);
        assertEquals(0.85 / 0.5, thermalKey, 1e-9);
        assertTrue(vppKey < thermalKey,
                "同性能指标下 VPP 排序键低于低价火电，排序靠后调用频次少");
    }

    @Test
    void 江苏口径不含K3抹平储能响应优势() {
        // 文章算例：储能 VPP K1=5.0 K2=0.98 K3=10.0；火电 K1=1.0 K2=0.85 K3=0.5
        double vppK12 = 5.0 * 0.98;
        double thermalK12 = 1.0 * 0.85;
        double ratioK12 = vppK12 / thermalK12;
        assertTrue(ratioK12 > 5.7 && ratioK12 < 5.9,
                "江苏口径 K=K1K2 储能约为火电 5.8 倍: " + ratioK12);
        double ratioFull = JiangsuFreqRanking.fullPerformance(5.0, 0.98, 10.0)
                / JiangsuFreqRanking.fullPerformance(1.0, 0.85, 0.5);
        assertTrue(ratioFull > 100,
                "完整 K=K1K2K3 口径差距 100 倍以上: " + ratioFull);
    }

    @Test
    void 江苏排序键分母必须为正() {
        assertThrows(IllegalArgumentException.class,
                () -> JiangsuFreqRanking.rankingKey(1.0, 1.0, null, 0.0, 8.0));
        assertThrows(IllegalArgumentException.class,
                () -> JiangsuFreqRanking.rankingKey(1.0, 1.0, null, null, 0.0));
    }

    @Test
    void 基线天气调整按温度灵敏度缩放() {
        // f(T)=1+0.05(T-25)：事件日 35°C → 1.5，基线 30°C → 1.25，系数 1.2
        double adjusted = BaselineHolidayAdjuster.adjustByWeather(1000, 35.0, 30.0);
        assertEquals(1200.0, adjusted, 0.01, "BLadj = BL · f(T_event)/f(T_baseline)");
        // 事件日比基线冷：系数 <1，基线下调
        assertTrue(BaselineHolidayAdjuster.adjustByWeather(1000, 28.0, 33.0) < 1000);
    }

    @Test
    void 基线天气调整灵敏度分母保护() {
        // f(5°C)=0：灵敏度回归参数异常必须拒绝计算
        assertThrows(IllegalArgumentException.class,
                () -> BaselineHolidayAdjuster.adjustByWeather(1000, 35.0, 5.0));
    }
}
