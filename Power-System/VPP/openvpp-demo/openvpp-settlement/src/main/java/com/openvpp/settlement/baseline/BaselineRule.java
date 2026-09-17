package com.openvpp.settlement.baseline;

/**
 * 基线核算规则 —— 版本化规则对象，结算追溯的锚点。
 * 每次核算记录规则版本，争议时可按同一版本重算复核；
 * 规则升级只影响新账期，不回溯改写已出账结果。
 */
public final class BaselineRule {

    private final String ruleVersion;
    private final String source;
    private final int minValidDays;
    private final double highOutlierFactor;
    private final int granularityMinutes;

    /**
     * @param ruleVersion        规则版本号
     * @param source             规则来源说明（教学场景必须明示"教学假设"）
     * @param minValidDays       最少有效样本日数，不足则拒绝核算
     * @param highOutlierFactor  高异常剔除倍数：高出池内中位数该倍数的样本日剔除
     * @param granularityMinutes 结算粒度（分钟），电网常规为 15
     */
    public BaselineRule(String ruleVersion, String source, int minValidDays,
                        double highOutlierFactor, int granularityMinutes) {
        this.ruleVersion = ruleVersion;
        this.source = source;
        this.minValidDays = minValidDays;
        this.highOutlierFactor = highOutlierFactor;
        this.granularityMinutes = granularityMinutes;
    }

    /**
     * 教学默认规则：仅用于演示口径，不对应任何地区的真实结算细则。
     * 真实项目必须以当地电网公司发布的现行结算规则文本为准逐项替换。
     */
    public static BaselineRule teachingDefault() {
        return new BaselineRule("TEACH-2026.1",
                "教学假设规则：相似日平均 + 响应前数据 + 高异常剔除", 3, 1.5, 15);
    }

    public String ruleVersion() {
        return ruleVersion;
    }

    public String source() {
        return source;
    }

    public int minValidDays() {
        return minValidDays;
    }

    public double highOutlierFactor() {
        return highOutlierFactor;
    }

    public int granularityMinutes() {
        return granularityMinutes;
    }
}
