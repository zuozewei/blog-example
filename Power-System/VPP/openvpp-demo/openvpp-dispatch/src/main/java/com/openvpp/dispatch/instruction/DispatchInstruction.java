package com.openvpp.dispatch.instruction;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 调度指令 —— 从聚合分解到设备执行的最小工作单元。
 * 不可变字段随创建固化，状态迁移只改 state 与五个可观测时标。
 *
 * 确认时间轴（每个时间点只证明一件事，互不替代）：
 *   sentAt       平台发送：指令已进入发送流程（44260 的 t_order）
 *   ackedAt      设备接收确认：设备收到并接受——只证明通信与受理完成
 *   actStartedAt 设备开始动作：设备开始执行
 *   reachedAt    遥测达到目标：实际功率进入目标容差带（t_action 只能以它为准）
 *   stableAt     连续稳定达标：指定窗口内持续满足，响应成功的唯一判据
 *
 * 注意：ACK 时间不能用于响应时延结算依据；响应时延 = reachedAt - sentAt。
 */
public class DispatchInstruction {

    private final String instructionId;
    private final String resourceId;
    private final BigDecimal commandKw;
    private final String targetProperty;
    private final BigDecimal targetValue;
    private final LocalDateTime createdAt;

    private InstructionState state;
    private LocalDateTime sentAt;
    private LocalDateTime ackedAt;
    private LocalDateTime actStartedAt;
    private LocalDateTime reachedAt;
    private LocalDateTime stableAt;

    public DispatchInstruction(String instructionId, String resourceId,
                               BigDecimal commandKw, String targetProperty,
                               BigDecimal targetValue) {
        this.instructionId = instructionId;
        this.resourceId = resourceId;
        this.commandKw = commandKw;
        this.targetProperty = targetProperty;
        this.targetValue = targetValue;
        this.createdAt = LocalDateTime.now();
        this.state = InstructionState.CREATED;
    }

    public void transitTo(InstructionState target) {
        state.assertTransitTo(target);
        state = target;
        switch (target) {
            case SENT: this.sentAt = LocalDateTime.now(); break;
            case ACKED: this.ackedAt = LocalDateTime.now(); break;
            case ACTING: this.actStartedAt = LocalDateTime.now(); break;
            case COMPLETED: this.stableAt = LocalDateTime.now(); break;
            default: break;
        }
    }

    /**
     * 遥测评估：实际功率是否进入容差带，并在稳定窗口内持续满足。
     * 离开容差带即重置 reachedAt——稳定必须连续，不允许断断续续凑窗口。
     *
     * @param measuredKw     遥测实测功率
     * @param toleranceKw    目标容差（kW）
     * @param stableWindowMs 连续稳定窗口（毫秒）
     * @return true 表示连续稳定达标，可转 COMPLETED
     */
    public boolean evaluateTelemetry(BigDecimal measuredKw, BigDecimal toleranceKw,
                                     long stableWindowMs) {
        boolean inBand = measuredKw.subtract(targetValue).abs().compareTo(toleranceKw) <= 0;
        if (!inBand) {
            this.reachedAt = null;
            return false;
        }
        if (this.reachedAt == null) {
            this.reachedAt = LocalDateTime.now();
        }
        return Duration.between(this.reachedAt, LocalDateTime.now()).toMillis() >= stableWindowMs;
    }

    /**
     * 遥测驱动的完成：遥测达标的证明力高于回执。
     * 回执丢失场景允许跨态补完（SENT/ACKED → COMPLETED），
     * 缺失的中间时标以达标时刻推断补齐并视为推断值。
     */
    public void completeByTelemetry() {
        LocalDateTime now = LocalDateTime.now();
        if (this.ackedAt == null) {
            this.ackedAt = now;      // ACK 丢失，以达标时刻推断
        }
        if (this.actStartedAt == null) {
            this.actStartedAt = now;
        }
        transitTo(InstructionState.COMPLETED);
    }

    /**
     * 通信确认耗时：sentAt → ackedAt。
     * 只反映"设备多久收到"，仅供参考，不能作为响应时延结算依据。
     */
    public Long ackElapsedMs() {
        if (sentAt == null || ackedAt == null) {
            return null;
        }
        return Duration.between(sentAt, ackedAt).toMillis();
    }

    /**
     * 44260 响应时间指标：t_action - t_order = reachedAt - sentAt。
     * 以遥测进入容差带为 t_action；未达标返回 null——ACK 不是响应。
     */
    public Long responseTimeMs() {
        if (sentAt == null || reachedAt == null) {
            return null;
        }
        return Duration.between(sentAt, reachedAt).toMillis();
    }

    /** 接收超时：SENT 态停留超过阈值（设备可能根本没收到） */
    public boolean isAckExpired(long ackTimeoutMs) {
        return state == InstructionState.SENT
                && sentAt != null
                && Duration.between(sentAt, LocalDateTime.now()).toMillis() > ackTimeoutMs;
    }

    /** 执行超时：已确认/已开始动作，但遥测迟迟不达标；计时起点取最近的执行时标 */
    public boolean isExecutionExpired(long execTimeoutMs) {
        LocalDateTime reference = actStartedAt != null ? actStartedAt : ackedAt;
        return (state == InstructionState.ACKED || state == InstructionState.ACTING)
                && reference != null
                && Duration.between(reference, LocalDateTime.now()).toMillis() > execTimeoutMs;
    }

    public String getInstructionId() { return instructionId; }
    public String getResourceId() { return resourceId; }
    public BigDecimal getCommandKw() { return commandKw; }
    public String getTargetProperty() { return targetProperty; }
    public BigDecimal getTargetValue() { return targetValue; }
    public InstructionState getState() { return state; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getSentAt() { return sentAt; }
    public LocalDateTime getAckedAt() { return ackedAt; }
    public LocalDateTime getActStartedAt() { return actStartedAt; }
    public LocalDateTime getReachedAt() { return reachedAt; }
    public LocalDateTime getStableAt() { return stableAt; }
}
