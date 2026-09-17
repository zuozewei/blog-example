package com.openvpp.resource.profile;

import com.openvpp.common.enums.ResourceType;

/**
 * 光伏资源档案 —— 单向输出，潜力靠预测。
 * 个性字段对应第 04 篇建模：辐照转换链的物理参数。
 */
public class PvResourceProfile extends ResourceProfile {

    /** 组件倾角（度），影响有效辐照计算 */
    private double tiltDeg;

    /** 功率温度系数（%/°C），单晶硅典型 -0.35 */
    private double tempCoeffPct = -0.35;

    /** 所在纬度，天文层辐照计算用 */
    private double latitude;

    public PvResourceProfile() {
        setType(ResourceType.DG);
    }

    public double getTiltDeg() {
        return tiltDeg;
    }

    public void setTiltDeg(double tiltDeg) {
        this.tiltDeg = tiltDeg;
    }

    public double getTempCoeffPct() {
        return tempCoeffPct;
    }

    public void setTempCoeffPct(double tempCoeffPct) {
        this.tempCoeffPct = tempCoeffPct;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
