package com.openvpp.settlement.assessment;

/**
 * 偏差考核器 —— 实际执行与申报偏差的惩罚计算。
 * 各省规则差异大（第 01 篇术语表），此处实现典型的分档考核：
 * - 合格率 ≥90%：不考核（达标）
 * - 70% ≤ 合格率 <90%：欠额部分按 1.0 倍单价考核
 * - 合格率 <70%：欠额部分按 1.5 倍单价加重考核（各省阶梯不同，配置化）
 *
 * 考核方向：只罚不足不奖超额——响应超发按申报量结算（封顶）。
 */
public class DeviationAssessor {

    private final double passThresholdPct;
    private final double penaltyThresholdPct;
    private final double penaltyRateNormal;
    private final double penaltyRateHeavy;

    public DeviationAssessor(double passThresholdPct, double penaltyThresholdPct,
                             double penaltyRateNormal, double penaltyRateHeavy) {
        this.passThresholdPct = passThresholdPct;
        this.penaltyThresholdPct = penaltyThresholdPct;
        this.penaltyRateNormal = penaltyRateNormal;
        this.penaltyRateHeavy = penaltyRateHeavy;
    }

    /** 省级典型配置：90% 达标线 / 70% 加重线 / 1.0 / 1.5 倍 */
    public static DeviationAssessor provincialDefault() {
        return new DeviationAssessor(90.0, 70.0, 1.0, 1.5);
    }

    /**
     * 计算考核费用（元）。
     *
     * @param passRatePct    响应合格率（%）
     * @param declaredKwh    申报响应量
     * @param priceYuanPerKwh 响应补贴单价
     * @return 考核费用（≥0，从应收补贴中扣除）
     */
    public double assess(double passRatePct, double declaredKwh, double priceYuanPerKwh) {
        if (passRatePct >= passThresholdPct) {
            return 0;
        }
        double shortfallKwh = declaredKwh * (1 - passRatePct / 100.0);
        if (passRatePct >= penaltyThresholdPct) {
            return shortfallKwh * priceYuanPerKwh * penaltyRateNormal;
        }
        return shortfallKwh * priceYuanPerKwh * penaltyRateHeavy;
    }

    /**
     * 应结算电量：达标按申报（封顶），不达标按实际。
     */
    public double settleKwh(double passRatePct, double declaredKwh, double actualKwh) {
        return passRatePct >= 100.0 ? declaredKwh : actualKwh;
    }
}
