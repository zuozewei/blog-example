package com.openvpp.market;

import com.openvpp.common.enums.Scenario;
import com.openvpp.market.bidding.BiddingStrategy;
import com.openvpp.market.bidding.PortfolioAllocator;
import com.openvpp.market.bidding.RiskAppetite;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 竞价策略单测：置信度报量 / 风险校验 / 分仓归一 / 超配压缩。
 */
class BiddingStrategyTest {

    private final BiddingStrategy strategy = new BiddingStrategy();
    private final PortfolioAllocator allocator = new PortfolioAllocator();

    @Test
    void 报量按容量置信度风险偏好三级折扣() {
        BigDecimal pool = new BigDecimal("1000");
        BigDecimal confidence = new BigDecimal("0.9");

        BigDecimal conservative = strategy.decideQuantity(pool, confidence, RiskAppetite.CONSERVATIVE);
        BigDecimal aggressive = strategy.decideQuantity(pool, confidence, RiskAppetite.AGGRESSIVE);

        // 保守：1000 × 0.9 × 0.7 = 630；激进：1000 × 0.9 × 1.0 = 900
        assertEquals(0, conservative.compareTo(new BigDecimal("630.000")));
        assertEquals(0, aggressive.compareTo(new BigDecimal("900.000")));
        assertTrue(conservative.compareTo(aggressive) < 0, "保守报量必须低于激进");
    }

    @Test
    void 低置信度自动缩量() {
        BigDecimal pool = new BigDecimal("1000");
        BigDecimal high = strategy.decideQuantity(pool, new BigDecimal("0.95"), RiskAppetite.AGGRESSIVE);
        BigDecimal low = strategy.decideQuantity(pool, new BigDecimal("0.40"), RiskAppetite.AGGRESSIVE);
        assertTrue(low.compareTo(high) < 0, "低置信度必须自动缩量");
        assertTrue(low.compareTo(pool) < 0, "缩量后仍不得超池");
    }

    @Test
    void 风险校验拦截激进报量() {
        // 置信度 0.5：一半概率做不到，考核单价 4.5 元 vs 预期收益 3 元
        boolean ok = strategy.riskAcceptable(
                new BigDecimal("1000"), new BigDecimal("3.0"),
                new BigDecimal("4.5"), new BigDecimal("0.5"));
        assertFalse(ok, "低置信度+高考核单价必须被拦截");

        // 置信度 0.95：同价格下风险可接受
        assertTrue(strategy.riskAcceptable(
                new BigDecimal("1000"), new BigDecimal("3.0"),
                new BigDecimal("4.5"), new BigDecimal("0.95")));
    }

    @Test
    void 分仓总和不超配原样通过() {
        Map<Scenario, BigDecimal> plan = new EnumMap<>(Scenario.class);
        plan.put(Scenario.ENERGY_MARKET, new BigDecimal("600"));
        plan.put(Scenario.FREQ_REG, new BigDecimal("400"));

        Map<Scenario, BigDecimal> normalized = allocator.normalize(plan, new BigDecimal("1000"));
        assertEquals(new BigDecimal("600"), normalized.get(Scenario.ENERGY_MARKET));
        assertEquals(new BigDecimal("400"), normalized.get(Scenario.FREQ_REG));
    }

    @Test
    void 超配按比例压缩防一货两卖() {
        Map<Scenario, BigDecimal> plan = new EnumMap<>(Scenario.class);
        plan.put(Scenario.ENERGY_MARKET, new BigDecimal("800"));
        plan.put(Scenario.FREQ_REG, new BigDecimal("800"));   // 总 1600 超配

        Map<Scenario, BigDecimal> normalized = allocator.normalize(plan, new BigDecimal("1000"));
        BigDecimal sum = normalized.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assertTrue(sum.compareTo(new BigDecimal("1000")) <= 0, "归一后总和不得超可信容量");
        // 等比压缩：各仓比例不变
        assertEquals(0, normalized.get(Scenario.ENERGY_MARKET)
                .compareTo(normalized.get(Scenario.FREQ_REG)));
    }
}
