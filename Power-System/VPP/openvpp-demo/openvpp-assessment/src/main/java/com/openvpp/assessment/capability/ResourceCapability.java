package com.openvpp.assessment.capability;

import com.openvpp.common.enums.Scenario;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资源能力评估结果 —— 对应 GB/T 44260 技术性评估指标全表（2.4 节）。
 * 每个资源每个评估周期产出一条记录，落库为时间序列：
 * 历史评估记录是策略复盘和基线修正的数据资产，不能算完就扔。
 */
public class ResourceCapability {

    private String resourceId;

    /** 评估时刻 */
    private LocalDateTime assessTime;

    /** 2.4.1 发电容量：输出有功功率最大值（负荷状态为负） */
    private BigDecimal genCapacity;

    /** 2.4.2 年发电量 */
    private BigDecimal annualEnergy;

    /** 2.4.3 调节容量：最大输出功率与最小输出功率之差，消耗功率为负 */
    private BigDecimal adjustCapacity;

    /** 2.4.4 响应时间：指令发出到功率按指令方向变化超阈值的耗时 */
    private Long responseTimeMs;

    /** 2.4.5 爬坡率：每分钟单方向功率变化量占调节容量的百分比 */
    private BigDecimal rampRate;

    /** 2.4.6 调节偏差率：实际与目标功率变化量的差值占比 */
    private BigDecimal deviationRate;

    /** 2.4.7 发电持续时间：达标且偏差维持在范围内的时长 */
    private Long sustainSeconds;

    /** 适用场景：PEAK_SHIFT / FREQ_REG / RESERVE / ENERGY_MARKET */
    private Scenario applicableScenario;

    /** 置信度折扣（工程实践扩展，非标准内容），聚合承诺时打折 */
    private BigDecimal confidence;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public LocalDateTime getAssessTime() {
        return assessTime;
    }

    public void setAssessTime(LocalDateTime assessTime) {
        this.assessTime = assessTime;
    }

    public BigDecimal getGenCapacity() {
        return genCapacity;
    }

    public void setGenCapacity(BigDecimal genCapacity) {
        this.genCapacity = genCapacity;
    }

    public BigDecimal getAnnualEnergy() {
        return annualEnergy;
    }

    public void setAnnualEnergy(BigDecimal annualEnergy) {
        this.annualEnergy = annualEnergy;
    }

    public BigDecimal getAdjustCapacity() {
        return adjustCapacity;
    }

    public void setAdjustCapacity(BigDecimal adjustCapacity) {
        this.adjustCapacity = adjustCapacity;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public BigDecimal getRampRate() {
        return rampRate;
    }

    public void setRampRate(BigDecimal rampRate) {
        this.rampRate = rampRate;
    }

    public BigDecimal getDeviationRate() {
        return deviationRate;
    }

    public void setDeviationRate(BigDecimal deviationRate) {
        this.deviationRate = deviationRate;
    }

    public Long getSustainSeconds() {
        return sustainSeconds;
    }

    public void setSustainSeconds(Long sustainSeconds) {
        this.sustainSeconds = sustainSeconds;
    }

    public Scenario getApplicableScenario() {
        return applicableScenario;
    }

    public void setApplicableScenario(Scenario applicableScenario) {
        this.applicableScenario = applicableScenario;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }
}
