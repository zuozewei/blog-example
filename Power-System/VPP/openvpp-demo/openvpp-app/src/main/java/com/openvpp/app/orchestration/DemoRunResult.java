package com.openvpp.app.orchestration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 园区需求响应贯穿案例的演示结果。
 * 关联标识 responseId 贯穿 任务/指令/基线/账单，供争议核查与追溯。
 */
public class DemoRunResult {

    private String responseId;
    private String path;                 // NORMAL / DEGRADED / DISPUTED
    private boolean feasible;            // 聚合可行性
    private BigDecimal gapKw = BigDecimal.ZERO;
    private BigDecimal baselineKw;       // 各时段基线均值
    private BigDecimal actualKw;         // 各时段实测均值
    private BigDecimal responseKwh;      // 实际响应电量
    private BigDecimal passRatePct;      // 合格率
    private BigDecimal settleYuan;       // 平台实收结算金额
    private BigDecimal penaltyYuan = BigDecimal.ZERO;
    private Map<String, BigDecimal> allocation;   // 用户分摊
    private List<String> trace;          // 关键步骤日志（手工核算底稿对照）
    private boolean idempotentReplay;    // 本次是否为幂等重放（未重复执行/出账）

    public String getResponseId() { return responseId; }
    public void setResponseId(String v) { this.responseId = v; }
    public String getPath() { return path; }
    public void setPath(String v) { this.path = v; }
    public boolean isFeasible() { return feasible; }
    public void setFeasible(boolean v) { this.feasible = v; }
    public BigDecimal getGapKw() { return gapKw; }
    public void setGapKw(BigDecimal v) { this.gapKw = v; }
    public BigDecimal getBaselineKw() { return baselineKw; }
    public void setBaselineKw(BigDecimal v) { this.baselineKw = v; }
    public BigDecimal getActualKw() { return actualKw; }
    public void setActualKw(BigDecimal v) { this.actualKw = v; }
    public BigDecimal getResponseKwh() { return responseKwh; }
    public void setResponseKwh(BigDecimal v) { this.responseKwh = v; }
    public BigDecimal getPassRatePct() { return passRatePct; }
    public void setPassRatePct(BigDecimal v) { this.passRatePct = v; }
    public BigDecimal getSettleYuan() { return settleYuan; }
    public void setSettleYuan(BigDecimal v) { this.settleYuan = v; }
    public BigDecimal getPenaltyYuan() { return penaltyYuan; }
    public void setPenaltyYuan(BigDecimal v) { this.penaltyYuan = v; }
    public Map<String, BigDecimal> getAllocation() { return allocation; }
    public void setAllocation(Map<String, BigDecimal> v) { this.allocation = v; }
    public List<String> getTrace() { return trace; }
    public void setTrace(List<String> v) { this.trace = v; }
    public boolean isIdempotentReplay() { return idempotentReplay; }
    public void setIdempotentReplay(boolean v) { this.idempotentReplay = v; }
}
