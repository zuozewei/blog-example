package com.openvpp.resource.profile;

import com.openvpp.common.enums.ResourceType;

import java.math.BigDecimal;

/**
 * 空调负荷资源档案 —— 仅下调，热惯性换时长。
 * 个性字段对应第 04 篇 ETP 模型：建筑热容/热阻、舒适温度带、重启保护。
 */
public class AcLoadResourceProfile extends ResourceProfile {

    /** 额定制冷功率 (kW) */
    private BigDecimal ratedCoolingKw;

    /** 建筑等效热容 (kJ/°C)，参数辨识而来，非铭牌值 */
    private BigDecimal thermalCapKjPerDeg;

    /** 建筑等效热阻 (°C/kW) */
    private BigDecimal thermalResistDegPerKw;

    /** 舒适度允许温升上限（°C） */
    private double maxTempRiseDeg = 1.5;

    /** 压缩机重启保护延时（秒），轮停周期必须大于它 */
    private int restartGuardSeconds = 240;

    public AcLoadResourceProfile() {
        setType(ResourceType.FL);
    }

    public BigDecimal getRatedCoolingKw() {
        return ratedCoolingKw;
    }

    public void setRatedCoolingKw(BigDecimal ratedCoolingKw) {
        this.ratedCoolingKw = ratedCoolingKw;
    }

    public BigDecimal getThermalCapKjPerDeg() {
        return thermalCapKjPerDeg;
    }

    public void setThermalCapKjPerDeg(BigDecimal thermalCapKjPerDeg) {
        this.thermalCapKjPerDeg = thermalCapKjPerDeg;
    }

    public BigDecimal getThermalResistDegPerKw() {
        return thermalResistDegPerKw;
    }

    public void setThermalResistDegPerKw(BigDecimal thermalResistDegPerKw) {
        this.thermalResistDegPerKw = thermalResistDegPerKw;
    }

    public double getMaxTempRiseDeg() {
        return maxTempRiseDeg;
    }

    public void setMaxTempRiseDeg(double maxTempRiseDeg) {
        this.maxTempRiseDeg = maxTempRiseDeg;
    }

    public int getRestartGuardSeconds() {
        return restartGuardSeconds;
    }

    public void setRestartGuardSeconds(int restartGuardSeconds) {
        this.restartGuardSeconds = restartGuardSeconds;
    }
}
