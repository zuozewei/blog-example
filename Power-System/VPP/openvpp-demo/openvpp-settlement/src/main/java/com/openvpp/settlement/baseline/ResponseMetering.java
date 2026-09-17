package com.openvpp.settlement.baseline;

/**
 * 响应量核定 —— 基线与实际曲线的差值积分。
 * 削峰场景：响应量 = Σ(基线 − 实际) × Δt，逐 15 分钟点累计。
 * 只累计正偏差（实际低于基线才算有效响应），负偏差不倒扣。
 */
public class ResponseMetering {

    /** 采样间隔（小时），电网标准 15 分钟点 */
    private static final double INTERVAL_HOURS = 0.25;

    /**
     * 核定响应电量（kWh）。
     *
     * @param baselineKw 各采样点基线负荷
     * @param actualKw   各采样点实际负荷，与基线等长
     */
    public double meter(double[] baselineKw, double[] actualKw) {
        if (baselineKw.length != actualKw.length) {
            throw new IllegalArgumentException("基线与实际点数不等长");
        }
        double responseKwh = 0;
        for (int i = 0; i < baselineKw.length; i++) {
            double delta = baselineKw[i] - actualKw[i];
            if (delta > 0) {
                responseKwh += delta * INTERVAL_HOURS;
            }
        }
        return responseKwh;
    }

    /**
     * 带数据质量检查的核定（表计数据可能存在缺失点）。
     * 任一侧缺失的点不计入响应量并显式记录；有效零值（实际负荷为 0）
     * 是全额响应，照常计入。缺失率超阈值的结果应转人工核查，不得直接出账。
     */
    public MeteringResult meterWithGapCheck(Double[] baselineKw, Double[] actualKw) {
        if (baselineKw.length != actualKw.length) {
            throw new IllegalArgumentException("基线与实际点数不等长");
        }
        double responseKwh = 0;
        int missing = 0;
        for (int i = 0; i < baselineKw.length; i++) {
            Double b = baselineKw[i];
            Double a = actualKw[i];
            if (b == null || a == null) {
                missing++;
                continue;
            }
            double delta = b - a;
            if (delta > 0) {
                responseKwh += delta * INTERVAL_HOURS;
            }
        }
        return new MeteringResult(responseKwh, baselineKw.length, missing);
    }

    /**
     * 响应合格率 = 实际响应量 / 申报响应量 × 100%。
     * 第 01 篇术语：一般要求 ≥80-90%，不达标可能被暂停资格。
     */
    public double passRate(double actualResponseKwh, double declaredResponseKwh) {
        if (declaredResponseKwh <= 0) {
            return 0;
        }
        return actualResponseKwh / declaredResponseKwh * 100.0;
    }
}
