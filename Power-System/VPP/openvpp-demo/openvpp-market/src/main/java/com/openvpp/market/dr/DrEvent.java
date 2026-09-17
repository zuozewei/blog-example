package com.openvpp.market.dr;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 需求响应事件 —— 一次 DR 业务的单据载体。
 * 贯穿申报/邀约/响应/结算全流程，金额与容量字段随阶段逐步补齐。
 */
public class DrEvent {

    private final String eventId;
    private final LocalDate responseDate;
    private final int startHour;
    private final int endHour;

    private DrEventState state;

    /** 申报阶段 */
    private BigDecimal declaredKwh;
    private BigDecimal declaredPrice;

    /** 响应阶段 */
    private BigDecimal actualKwh;

    /** 结算阶段 */
    private BigDecimal passRatePct;
    private BigDecimal penaltyYuan;
    private BigDecimal settleYuan;

    private final LocalDateTime createdAt;

    public DrEvent(String eventId, LocalDate responseDate, int startHour, int endHour) {
        this.eventId = eventId;
        this.responseDate = responseDate;
        this.startHour = startHour;
        this.endHour = endHour;
        this.state = DrEventState.DECLARED;
        this.createdAt = LocalDateTime.now();
    }

    public void transitTo(DrEventState target) {
        state.assertTransitTo(target);
        state = target;
    }

    public String getEventId() { return eventId; }
    public LocalDate getResponseDate() { return responseDate; }
    public int getStartHour() { return startHour; }
    public int getEndHour() { return endHour; }
    public DrEventState getState() { return state; }
    public BigDecimal getDeclaredKwh() { return declaredKwh; }
    public void setDeclaredKwh(BigDecimal declaredKwh) { this.declaredKwh = declaredKwh; }
    public BigDecimal getDeclaredPrice() { return declaredPrice; }
    public void setDeclaredPrice(BigDecimal declaredPrice) { this.declaredPrice = declaredPrice; }
    public BigDecimal getActualKwh() { return actualKwh; }
    public void setActualKwh(BigDecimal actualKwh) { this.actualKwh = actualKwh; }
    public BigDecimal getPassRatePct() { return passRatePct; }
    public void setPassRatePct(BigDecimal passRatePct) { this.passRatePct = passRatePct; }
    public BigDecimal getPenaltyYuan() { return penaltyYuan; }
    public void setPenaltyYuan(BigDecimal penaltyYuan) { this.penaltyYuan = penaltyYuan; }
    public BigDecimal getSettleYuan() { return settleYuan; }
    public void setSettleYuan(BigDecimal settleYuan) { this.settleYuan = settleYuan; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
