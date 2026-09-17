package com.openvpp.dispatch;

import com.openvpp.common.enums.ResourceType;
import com.openvpp.common.enums.Scenario;
import com.openvpp.dispatch.strategy.DispatchRule;
import com.openvpp.dispatch.strategy.RuleEngine;
import com.openvpp.dispatch.strategy.StrategyExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 策略引擎全场景单测：
 * 场景匹配 / 类型拦截 / 能力上限 / 涉控边界 / 代理失效 / 区域规则叠加。
 */
class StrategyEngineTest {

    private RuleEngine engine;
    private StrategyExecutor executor;

    @BeforeEach
    void setUp() {
        engine = new RuleEngine();
        executor = new StrategyExecutor(engine);
    }

    @Test
    void 内置底线规则覆盖三大场景() {
        assertEquals(3, engine.ruleCount());
        assertTrue(engine.match(Scenario.FREQ_REG).isPresent());
        assertTrue(engine.match(Scenario.PEAK_SHIFT).isPresent());
        assertTrue(engine.match(Scenario.RESERVE).isPresent());
    }

    @Test
    void 调频只允许储能空调被拦截() {
        Optional<String> violation = engine.check(Scenario.FREQ_REG,
                ResourceType.FL, 5_000, 600);
        assertTrue(violation.isPresent());
        assertTrue(violation.get().contains("资源类型不允许"));

        assertTrue(engine.check(Scenario.FREQ_REG,
                ResourceType.ES, 5_000, 600).isEmpty(), "储能应被调频放行");
    }

    @Test
    void 响应超时被拦截() {
        Optional<String> violation = engine.check(Scenario.FREQ_REG,
                ResourceType.ES, 120_000, 600);   // 2 分钟 > 1 分钟上限
        assertTrue(violation.isPresent());
        assertTrue(violation.get().contains("响应超时"));
    }

    @Test
    void 指令超可信容量被拦截() {
        StrategyExecutor.Decision decision = executor.evaluate(
                Scenario.PEAK_SHIFT, ResourceType.FL, 60_000, 3600,
                new BigDecimal("1500"), new BigDecimal("1000"), true, false);
        assertFalse(decision.isPassed());
        assertTrue(decision.getReason().contains("超可信容量"));
    }

    @Test
    void 涉控指令仅限调频备用场景() {
        StrategyExecutor.Decision decision = executor.evaluate(
                Scenario.PEAK_SHIFT, ResourceType.FL, 60_000, 3600,
                new BigDecimal("500"), new BigDecimal("1000"), true, true);
        assertFalse(decision.isPassed());
        assertTrue(decision.getReason().contains("涉控"));

        StrategyExecutor.Decision pass = executor.evaluate(
                Scenario.FREQ_REG, ResourceType.ES, 5_000, 600,
                new BigDecimal("500"), new BigDecimal("1000"), true, true);
        assertTrue(pass.isPassed(), "调频涉控应放行");
    }

    @Test
    void 代理失效被拦截() {
        StrategyExecutor.Decision decision = executor.evaluate(
                Scenario.PEAK_SHIFT, ResourceType.FL, 60_000, 3600,
                new BigDecimal("500"), new BigDecimal("1000"), false, false);
        assertFalse(decision.isPassed());
        assertTrue(decision.getReason().contains("代理"));
    }

    @Test
    void 区域规则可加严不可放松() {
        // 华北区域加严：调峰响应上限从 15 分钟收紧到 10 分钟
        engine.register(new DispatchRule("huabei-peak-strict", Scenario.PEAK_SHIFT,
                EnumSet.of(ResourceType.FL, ResourceType.ES),
                600_000, 1800, -1));   // 更高优先级（数值更小）

        Optional<String> violation = engine.check(Scenario.PEAK_SHIFT,
                ResourceType.FL, 720_000, 3600);   // 12 分钟：国标放行但华北拦截
        assertTrue(violation.isPresent(), "区域加严规则应优先生效");
    }

    @Test
    void 全关通过放行() {
        StrategyExecutor.Decision decision = executor.evaluate(
                Scenario.PEAK_SHIFT, ResourceType.ES, 300_000, 3600,
                new BigDecimal("800"), new BigDecimal("1000"), true, false);
        assertTrue(decision.isPassed());
        assertNull(decision.getReason());
    }
}
