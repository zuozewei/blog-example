package com.openvpp.market;

import com.openvpp.market.dr.DrEvent;
import com.openvpp.market.dr.DrEventState;
import com.openvpp.market.dr.DrOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 需求响应全流程单测：正常流转 / 非法跳转 / 中途取消 / 结算金额落账。
 */
class DrOrchestratorTest {

    private DrOrchestrator orchestrator;
    private static final LocalDate DATE = LocalDate.of(2026, 9, 17);

    @BeforeEach
    void setUp() {
        orchestrator = new DrOrchestrator();
    }

    @Test
    void 全流程八态走通() {
        orchestrator.declare("dr-001", DATE, 14, 16,
                new BigDecimal("500"), new BigDecimal("3.0"));
        orchestrator.award("dr-001");
        orchestrator.invite("dr-001");
        orchestrator.confirm("dr-001");
        orchestrator.startResponse("dr-001");
        orchestrator.complete("dr-001", new BigDecimal("460"));
        orchestrator.settle("dr-001", new BigDecimal("92.0"),
                BigDecimal.ZERO, new BigDecimal("1380"));

        DrEvent event = orchestrator.require("dr-001");
        assertEquals(DrEventState.SETTLED, event.getState());
        assertEquals(new BigDecimal("460"), event.getActualKwh());
        assertEquals(new BigDecimal("1380"), event.getSettleYuan());
    }

    @Test
    void 非法跳态被拦截() {
        orchestrator.declare("dr-002", DATE, 14, 16,
                new BigDecimal("500"), new BigDecimal("3.0"));
        // DECLARED 不能直接 CONFIRMED
        assertThrows(IllegalStateException.class,
                () -> orchestrator.confirm("dr-002"));
        // 未邀约不能开始响应
        assertThrows(IllegalStateException.class,
                () -> orchestrator.startResponse("dr-002"));
    }

    @Test
    void 邀约未确认可取消() {
        orchestrator.declare("dr-003", DATE, 14, 16,
                new BigDecimal("500"), new BigDecimal("3.0"));
        orchestrator.award("dr-003");
        orchestrator.invite("dr-003");
        orchestrator.cancel("dr-003", "用户未在时限内确认");

        assertEquals(DrEventState.CANCELLED, orchestrator.require("dr-003").getState());
    }

    @Test
    void 终态不可逆() {
        orchestrator.declare("dr-004", DATE, 14, 16,
                new BigDecimal("500"), new BigDecimal("3.0"));
        orchestrator.cancel("dr-004", "流标");
        assertThrows(IllegalStateException.class,
                () -> orchestrator.award("dr-004"), "已取消事件不得再中标");
    }

    @Test
    void 结算必须基于完成态() {
        orchestrator.declare("dr-005", DATE, 14, 16,
                new BigDecimal("500"), new BigDecimal("3.0"));
        orchestrator.award("dr-005");
        // AWARDED 不能跳 SETTLED
        assertThrows(IllegalStateException.class, () ->
                orchestrator.settle("dr-005", new BigDecimal("100"),
                        BigDecimal.ZERO, new BigDecimal("1500")));
    }
}
