package com.openvpp.settlement.baseline;

import java.time.LocalDate;

/**
 * 基线样本点：某候选相似日在同一结算时段的负荷计量。
 * loadKw 为 null 表示该点计量缺失；0 是有效计量值（真实零负荷），不得当缺失处理。
 */
public final class SamplePoint {

    private final LocalDate date;
    private final Double loadKw;

    public SamplePoint(LocalDate date, Double loadKw) {
        this.date = date;
        this.loadKw = loadKw;
    }

    public LocalDate date() {
        return date;
    }

    public Double loadKw() {
        return loadKw;
    }
}
