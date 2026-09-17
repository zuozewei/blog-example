package com.openvpp.market.dr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 需求响应流程编排器 —— DR 业务的状态机驱动核心。
 * 每一步流转都有业务校验：申报要容量价格、确认要邀约在先、
 * 完成要实际响应量、结算要合格率与考核。
 * 编排器只驱动状态与单据，具体聚合/指令/基线调用由上层服务注入。
 */
public class DrOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(DrOrchestrator.class);

    private final Map<String, DrEvent> events = new HashMap<>();

    /** 申报：录入容量与报价，事件建档 */
    public DrEvent declare(String eventId, java.time.LocalDate date, int startHour, int endHour,
                           BigDecimal declaredKwh, BigDecimal declaredPrice) {
        DrEvent event = new DrEvent(eventId, date, startHour, endHour);
        event.setDeclaredKwh(declaredKwh);
        event.setDeclaredPrice(declaredPrice);
        events.put(eventId, event);
        log.info("DR 申报: {} {} {}-{} 时, {} kWh @ {} 元/kWh",
                eventId, date, startHour, endHour, declaredKwh, declaredPrice);
        return event;
    }

    /** 中标：DECLARED → AWARDED */
    public void award(String eventId) {
        require(eventId).transitTo(DrEventState.AWARDED);
    }

    /** 发起邀约：AWARDED → INVITED */
    public void invite(String eventId) {
        require(eventId).transitTo(DrEventState.INVITED);
    }

    /** 用户确认：INVITED → CONFIRMED */
    public void confirm(String eventId) {
        require(eventId).transitTo(DrEventState.CONFIRMED);
    }

    /** 开始执行：CONFIRMED → RESPONDING */
    public void startResponse(String eventId) {
        require(eventId).transitTo(DrEventState.RESPONDING);
    }

    /** 执行完成：录入实际响应量，RESPONDING → COMPLETED */
    public void complete(String eventId, BigDecimal actualKwh) {
        DrEvent event = require(eventId);
        event.setActualKwh(actualKwh);
        event.transitTo(DrEventState.COMPLETED);
    }

    /** 结算：录入合格率/考核/金额，COMPLETED → SETTLED */
    public void settle(String eventId, BigDecimal passRatePct,
                       BigDecimal penaltyYuan, BigDecimal settleYuan) {
        DrEvent event = require(eventId);
        event.setPassRatePct(passRatePct);
        event.setPenaltyYuan(penaltyYuan);
        event.setSettleYuan(settleYuan);
        event.transitTo(DrEventState.SETTLED);
        log.info("DR 结算: {} 合格率 {}% 考核 {} 元 结算 {} 元",
                eventId, passRatePct, penaltyYuan, settleYuan);
    }

    /** 取消/流标：任意活动态 → CANCELLED */
    public void cancel(String eventId, String reason) {
        require(eventId).transitTo(DrEventState.CANCELLED);
        log.warn("DR 取消: {} 原因: {}", eventId, reason);
    }

    public DrEvent require(String eventId) {
        DrEvent event = events.get(eventId);
        if (event == null) {
            throw new IllegalArgumentException("未登记的 DR 事件: " + eventId);
        }
        return event;
    }
}
