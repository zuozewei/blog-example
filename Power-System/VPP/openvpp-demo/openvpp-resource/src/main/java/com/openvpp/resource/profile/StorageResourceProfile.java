package com.openvpp.resource.profile;

import com.openvpp.common.enums.ResourceType;

import java.math.BigDecimal;

/**
 * 储能资源档案 —— 双向调节，SOC 约束。
 * 个性字段对应第 04 篇：PCS 功率、额定容量、SOC 上下限、效率、站用电率。
 */
public class StorageResourceProfile extends ResourceProfile {

    /** PCS 额定功率 (kW)，双向对称 */
    private BigDecimal pcsRateKw;

    /** 额定容量 (kWh) */
    private BigDecimal ratedKwh;

    /** SOC 下限（%），44260 评估的实际可用边界 */
    private double socMinPct = 10.0;

    /** SOC 上限（%） */
    private double socMaxPct = 90.0;

    /** 充放电综合效率（%） */
    private double efficiencyPct = 95.0;

    /** 站用电率（%），可承诺容量须扣除 */
    private double stationUsePct = 4.0;

    public StorageResourceProfile() {
        setType(ResourceType.ES);
    }

    public BigDecimal getPcsRateKw() {
        return pcsRateKw;
    }

    public void setPcsRateKw(BigDecimal pcsRateKw) {
        this.pcsRateKw = pcsRateKw;
    }

    public BigDecimal getRatedKwh() {
        return ratedKwh;
    }

    public void setRatedKwh(BigDecimal ratedKwh) {
        this.ratedKwh = ratedKwh;
    }

    public double getSocMinPct() {
        return socMinPct;
    }

    public void setSocMinPct(double socMinPct) {
        this.socMinPct = socMinPct;
    }

    public double getSocMaxPct() {
        return socMaxPct;
    }

    public void setSocMaxPct(double socMaxPct) {
        this.socMaxPct = socMaxPct;
    }

    public double getEfficiencyPct() {
        return efficiencyPct;
    }

    public void setEfficiencyPct(double efficiencyPct) {
        this.efficiencyPct = efficiencyPct;
    }

    public double getStationUsePct() {
        return stationUsePct;
    }

    public void setStationUsePct(double stationUsePct) {
        this.stationUsePct = stationUsePct;
    }
}
