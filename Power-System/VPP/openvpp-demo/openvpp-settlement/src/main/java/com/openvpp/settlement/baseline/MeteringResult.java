package com.openvpp.settlement.baseline;

/**
 * 响应量核定结果：响应电量 + 数据质量。
 * 缺失点不计入响应量，但必须显式记录——缺失率超阈值时结果不可直接用于结算，转人工核查。
 */
public final class MeteringResult {

    private final double responseKwh;
    private final int totalPoints;
    private final int missingPoints;

    public MeteringResult(double responseKwh, int totalPoints, int missingPoints) {
        this.responseKwh = responseKwh;
        this.totalPoints = totalPoints;
        this.missingPoints = missingPoints;
    }

    public double responseKwh() {
        return responseKwh;
    }

    public int totalPoints() {
        return totalPoints;
    }

    public int missingPoints() {
        return missingPoints;
    }

    /** 缺失率 = 缺失点数 / 总点数 */
    public double missingRatio() {
        return totalPoints == 0 ? 0 : (double) missingPoints / totalPoints;
    }
}
