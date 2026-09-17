package com.openvpp.dispatch.strategy;

import com.openvpp.common.enums.ResourceType;
import com.openvpp.common.enums.Scenario;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 规则引擎 —— 策略决策的求值核心。
 * 职责收敛为两件事：
 * 1. 按场景+优先级匹配适用规则（小数值优先）；
 * 2. 对候选资源执行规则校验（类型/响应时间/持续时长三关）。
 *
 * 44260 的场景约束在引擎里固化为内置规则，与区域配置规则共存：
 * 内置规则是国标底线（不可删），配置规则是区域加严（可叠不可松）。
 */
public class RuleEngine {

    private final List<DispatchRule> rules = new ArrayList<>();

    public RuleEngine() {
        registerBuiltin();
    }

    /** 44260 内置底线规则：调频只放储能、调峰负荷为主、备用要确定性资源 */
    private void registerBuiltin() {
        // 调频：秒级响应 + 负反馈，只允许储能（ES），1 分钟响应上限
        register(new DispatchRule("builtin-freq-reg", Scenario.FREQ_REG,
                java.util.EnumSet.of(ResourceType.ES),
                60_000, 300, 0));
        // 调峰/需求响应：负荷为主 + 储能，15 分钟响应，30 分钟持续
        register(new DispatchRule("builtin-peak-shift", Scenario.PEAK_SHIFT,
                java.util.EnumSet.of(ResourceType.FL, ResourceType.ES),
                900_000, 1800, 0));
        // 备用：确定性调节容量资源（储能），1 小时持续
        register(new DispatchRule("builtin-reserve", Scenario.RESERVE,
                java.util.EnumSet.of(ResourceType.ES),
                300_000, 3600, 0));
    }

    public synchronized void register(DispatchRule rule) {
        rules.add(rule);
        rules.sort(Comparator.comparingInt(DispatchRule::getPriority));
    }

    /** 按场景匹配最高优先级规则 */
    public Optional<DispatchRule> match(Scenario scenario) {
        return rules.stream()
                .filter(r -> r.getScenario() == scenario)
                .findFirst();
    }

    /**
     * 校验候选资源是否满足规则三关。
     * 返回违规原因；通过返回空。
     */
    public Optional<String> check(Scenario scenario, ResourceType type,
                                  long responseTimeMs, long sustainSeconds) {
        Optional<DispatchRule> rule = match(scenario);
        if (rule.isEmpty()) {
            return Optional.of("无适用规则: " + scenario);
        }
        DispatchRule r = rule.get();
        if (!r.getAllowedTypes().contains(type)) {
            return Optional.of("资源类型不允许: " + type + " 不得参与 " + scenario);
        }
        if (responseTimeMs > r.getMaxResponseTimeMs()) {
            return Optional.of("响应超时: " + responseTimeMs + "ms > " + r.getMaxResponseTimeMs() + "ms");
        }
        if (sustainSeconds < r.getMinSustainSeconds()) {
            return Optional.of("持续时长不足: " + sustainSeconds + "s < " + r.getMinSustainSeconds() + "s");
        }
        return Optional.empty();
    }

    public int ruleCount() {
        return rules.size();
    }
}
