package com.openvpp.market.dr;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 需求响应事件状态 —— 一次 DR 业务从申报到结算的完整生命周期。
 *
 * 状态图：
 *   DECLARED(已申报) ──中标──> AWARDED(已中标) ──发起邀约──> INVITED(已邀约)
 *   INVITED ──用户确认──> CONFIRMED(已确认) ──执行──> RESPONDING(响应中)
 *   RESPONDING ──执行结束──> COMPLETED(已完成) ──基线核算──> SETTLED(已结算)
 *
 *   任意活动态 ──取消/流标──> CANCELLED(已取消)
 *
 * 与指令状态机（第 15 篇）的分工：指令管"单设备执行"，
 * 本状态机管"整单业务流转"——一个 DR 事件拆解成 N 条调度指令。
 */
public enum DrEventState {

    DECLARED, AWARDED, INVITED, CONFIRMED, RESPONDING, COMPLETED, SETTLED, CANCELLED;

    private static final Map<DrEventState, Set<DrEventState>> LEGAL = Map.of(
            DECLARED, EnumSet.of(AWARDED, CANCELLED),
            AWARDED, EnumSet.of(INVITED, CANCELLED),
            INVITED, EnumSet.of(CONFIRMED, CANCELLED),
            CONFIRMED, EnumSet.of(RESPONDING, CANCELLED),
            RESPONDING, EnumSet.of(COMPLETED, CANCELLED),
            COMPLETED, EnumSet.of(SETTLED),
            SETTLED, EnumSet.noneOf(DrEventState.class),
            CANCELLED, EnumSet.noneOf(DrEventState.class)
    );

    public void assertTransitTo(DrEventState target) {
        if (!LEGAL.get(this).contains(target)) {
            throw new IllegalStateException("非法 DR 事件状态迁移: " + this + " → " + target);
        }
    }
}
