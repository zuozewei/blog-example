package com.openvpp.assessment.pv;

import java.time.LocalDateTime;

/**
 * 光伏出力预测曲线 —— 单时刻预测值 + 概率区间。
 * 44260 评估的产出不是"一个数"，而是"一个数加它能兑现的概率"：
 * P_50 用于日前计划，P_90（悲观）用于保守申报，区间宽度即风险量化。
 */
public class ForecastCurve {

    private final LocalDateTime time;

    /** 中位数预测（最可能值） */
    private final double p50Kw;

    /** 悲观分位（90% 概率实际出力不低于此值） */
    private final double p90Kw;

    /** 乐观分位（90% 概率实际出力不高于此值） */
    private final double p10Kw;

    public ForecastCurve(LocalDateTime time, double p50Kw, double p90Kw, double p10Kw) {
        this.time = time;
        this.p50Kw = p50Kw;
        this.p90Kw = p90Kw;
        this.p10Kw = p10Kw;
    }

    /** 预测区间宽度（kW），风险量化的直接读数 */
    public double uncertaintyWidth() {
        return p10Kw - p90Kw;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public double getP50Kw() {
        return p50Kw;
    }

    public double getP90Kw() {
        return p90Kw;
    }

    public double getP10Kw() {
        return p10Kw;
    }
}
