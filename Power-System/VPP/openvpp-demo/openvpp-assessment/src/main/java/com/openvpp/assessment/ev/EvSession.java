package com.openvpp.assessment.ev;

/**
 * EV 接入会话 —— 六边形可行域计算的输入参数（专栏第 35 篇）。
 *
 * EV 比储能（DESS）多 3 个约束：接入时段 [ts, td]、离网 SOC ≥ Se、SOC 下限 20%（防过放）。
 * 「车是有主人的电池」——出行约束是 EV 建模与 DESS 的本质差异。
 */
public final class EvSession {

    /** SOC 下限：20%，防过放 */
    public static final double SOC_FLOOR_PCT = 20.0;
    /** SOC 上限：100% */
    public static final double SOC_CEILING_PCT = 100.0;

    private final double durationHours;
    private final double arrivalSocPct;
    private final double departRequiredSocPct;
    private final double capacityKwh;
    private final double ratedKw;
    private final double efficiency;

    /**
     * @param durationHours        接入时长 td - ts（小时）
     * @param arrivalSocPct        接入 SOC Ss（%）
     * @param departRequiredSocPct 离网要求 SOC Se（%）
     * @param capacityKwh          电池容量（kWh）
     * @param ratedKw              额定充/放电功率（kW，对称假设）
     * @param efficiency           充放电综合效率（0~1）
     */
    public EvSession(double durationHours, double arrivalSocPct, double departRequiredSocPct,
                     double capacityKwh, double ratedKw, double efficiency) {
        if (durationHours <= 0) {
            throw new IllegalArgumentException("接入时长必须为正: " + durationHours);
        }
        if (capacityKwh <= 0 || ratedKw <= 0) {
            throw new IllegalArgumentException("容量与额定功率必须为正");
        }
        if (efficiency <= 0 || efficiency > 1.0) {
            throw new IllegalArgumentException("效率必须在 (0, 1] 区间: " + efficiency);
        }
        this.durationHours = durationHours;
        this.arrivalSocPct = arrivalSocPct;
        this.departRequiredSocPct = departRequiredSocPct;
        this.capacityKwh = capacityKwh;
        this.ratedKw = ratedKw;
        this.efficiency = efficiency;
    }

    public double durationHours() { return durationHours; }
    public double arrivalSocPct() { return arrivalSocPct; }
    public double departRequiredSocPct() { return departRequiredSocPct; }
    public double capacityKwh() { return capacityKwh; }
    public double ratedKw() { return ratedKw; }
    public double efficiency() { return efficiency; }

    /** 满功率充电速率（SOC%/h） */
    public double chargeRatePctPerHour() {
        return ratedKw * efficiency / capacityKwh * 100.0;
    }

    /** 满功率放电速率（SOC%/h） */
    public double dischargeRatePctPerHour() {
        return ratedKw * efficiency / capacityKwh * 100.0;
    }
}
