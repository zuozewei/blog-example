package com.openvpp.resource.profile;

import com.openvpp.common.enums.ResourceType;

import java.math.BigDecimal;

/**
 * 充电桩资源档案 —— 建模到"站"，车/桩会话是瞬态子实体（第 04 篇决策）。
 * 个性字段：桩数、单桩功率、V2G 支持、车辆类型构成。
 */
public class EvChargerResourceProfile extends ResourceProfile {

    /** 桩数 */
    private int pileCount;

    /** 单桩额定功率 (kW) */
    private BigDecimal pileRateKw;

    /** 是否支持 V2G 双向放电 */
    private boolean v2gCapable;

    /** 车辆类型构成快照：如 "网约车40/私家35/物流15/出租10"，行为模型入参 */
    private String vehicleMix;

    public EvChargerResourceProfile() {
        setType(ResourceType.FL);
    }

    public int getPileCount() {
        return pileCount;
    }

    public void setPileCount(int pileCount) {
        this.pileCount = pileCount;
    }

    public BigDecimal getPileRateKw() {
        return pileRateKw;
    }

    public void setPileRateKw(BigDecimal pileRateKw) {
        this.pileRateKw = pileRateKw;
    }

    public boolean isV2gCapable() {
        return v2gCapable;
    }

    public void setV2gCapable(boolean v2gCapable) {
        this.v2gCapable = v2gCapable;
    }

    public String getVehicleMix() {
        return vehicleMix;
    }

    public void setVehicleMix(String vehicleMix) {
        this.vehicleMix = vehicleMix;
    }
}
