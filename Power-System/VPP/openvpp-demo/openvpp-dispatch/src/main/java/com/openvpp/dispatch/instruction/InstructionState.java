package com.openvpp.dispatch.instruction;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 指令状态 —— 调度指令生命周期的状态机。
 *
 * 确认时间轴（五个可观测时间点，各自语义独立）：
 *   平台发送 sentAt → 设备接收确认 ackedAt → 设备开始动作 actStartedAt
 *   → 遥测达到目标 reachedAt → 连续稳定达标 stableAt
 *
 * 状态图：
 *   CREATED ──下发──> SENT ──设备确认──> ACKED ──开始动作──> ACTING
 *      │                │ 接收超时         │ 执行超时         │ 执行超时
 *      │                ▼                 ▼                 ▼
 *      │             REVIEW（超时且执行结果未知，转人工核查，禁止直接判失败重发）
 *      │                │
 *      │                ├── 迟到遥测证明达标 ──> COMPLETED（响应成功）
 *      │                └── 核查确认未执行 ────> FAILED
 *      │
 *      └── 任意非终态可被 CANCELLED 抢占：人工接管 > 本地保护 > 平台控制
 *
 * 核心规则：
 * 1. COMPLETED 只能由遥测连续稳定达标驱动——ACK 仅代表"设备收到并接受"，
 *    不得作为响应成功或响应时延结算的依据；
 * 2. 遥测达标的证明力高于回执：回执丢失时允许从 SENT/ACKED 跨态补完 COMPLETED；
 * 3. 终态（COMPLETED/FAILED/CANCELLED）后迟到的消息一律忽略，不得重新激活执行。
 */
public enum InstructionState {

    /** 已创建：指令已生成并持久化，待下发 */
    CREATED,

    /** 已下发：已进入发送流程，等设备接收确认（接收超时阈值管辖） */
    SENT,

    /** 已确认：设备收到并接受指令——只证明通信与受理完成，不证明功率到位 */
    ACKED,

    /** 执行中：设备已开始动作，等遥测达标（执行超时阈值管辖） */
    ACTING,

    /** 已完成：遥测在指定窗口内连续稳定达标，响应成功（终态） */
    COMPLETED,

    /** 待核查：超时且执行结果未知——设备可能已执行，转人工核查而非简单判失败 */
    REVIEW,

    /** 已失败：设备明确拒绝，或核查确认未执行（终态） */
    FAILED,

    /** 已取消：人工接管 / 本地保护 / 平台撤销（终态） */
    CANCELLED;

    private static final Map<InstructionState, Set<InstructionState>> LEGAL_TRANSITIONS = Map.of(
            CREATED, EnumSet.of(SENT, FAILED, CANCELLED),
            SENT, EnumSet.of(ACKED, ACTING, COMPLETED, REVIEW, FAILED, CANCELLED),
            ACKED, EnumSet.of(ACTING, COMPLETED, REVIEW, FAILED, CANCELLED),
            ACTING, EnumSet.of(COMPLETED, REVIEW, FAILED, CANCELLED),
            REVIEW, EnumSet.of(COMPLETED, FAILED, CANCELLED),
            COMPLETED, EnumSet.noneOf(InstructionState.class),
            FAILED, EnumSet.noneOf(InstructionState.class),
            CANCELLED, EnumSet.noneOf(InstructionState.class)
    );

    public boolean canTransitTo(InstructionState target) {
        return LEGAL_TRANSITIONS.get(this).contains(target);
    }

    public void assertTransitTo(InstructionState target) {
        if (!canTransitTo(target)) {
            throw new IllegalStateException(
                    "非法指令状态迁移: " + this + " → " + target);
        }
    }

    /** 终态：COMPLETED / FAILED / CANCELLED，迟到消息不得重新激活 */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }
}
