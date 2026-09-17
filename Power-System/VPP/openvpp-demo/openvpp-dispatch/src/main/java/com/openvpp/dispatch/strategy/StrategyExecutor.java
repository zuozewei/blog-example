package com.openvpp.dispatch.strategy;

import com.openvpp.common.enums.ResourceType;
import com.openvpp.common.enums.Scenario;

import java.math.BigDecimal;

/**
 * 策略执行框架 —— 指令下发前的最后一道闸门（47241 第 9.4 条控制校核的落点）。
 *
 * 四道关，全部过才放行：
 * 1. 场景-资源匹配（RuleEngine 三关校验）；
 * 2. 能力上限：指令不得超过资源当前可信容量；
 * 3. 安全边界：涉控指令须带二次确认标记（防误动）；
 * 4. 代理有效：资源当前在代理期内。
 *
 * 任何一关不过都返回带原因的拒绝，而不是静默丢弃——
 * 控制校核的价值就在于"拒绝时说清楚为什么"。
 */
public class StrategyExecutor {

    private final RuleEngine ruleEngine;

    public StrategyExecutor(RuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    /**
     * 指令准入评估。
     */
    public Decision evaluate(Scenario scenario, ResourceType type,
                             long responseTimeMs, long sustainSeconds,
                             BigDecimal commandKw, BigDecimal credibleCapacityKw,
                             boolean contractValid, boolean criticalControl) {
        // 关 1：场景-资源匹配
        var violation = ruleEngine.check(scenario, type, responseTimeMs, sustainSeconds);
        if (violation.isPresent()) {
            return Decision.reject(violation.get());
        }
        // 关 2：能力上限
        if (commandKw.compareTo(credibleCapacityKw) > 0) {
            return Decision.reject("指令超可信容量: " + commandKw + " > " + credibleCapacityKw);
        }
        // 关 3：涉控安全边界
        if (criticalControl && scenario != Scenario.FREQ_REG && scenario != Scenario.RESERVE) {
            return Decision.reject("涉控指令仅允许调频/备用场景");
        }
        // 关 4：代理有效
        if (!contractValid) {
            return Decision.reject("资源代理已失效");
        }
        return Decision.pass();
    }

    /** 评估结论：放行或带原因拒绝 */
    public static class Decision {
        private final boolean passed;
        private final String reason;

        private Decision(boolean passed, String reason) {
            this.passed = passed;
            this.reason = reason;
        }

        public static Decision pass() {
            return new Decision(true, null);
        }

        public static Decision reject(String reason) {
            return new Decision(false, reason);
        }

        public boolean isPassed() { return passed; }
        public String getReason() { return reason; }
    }
}
