package com.openvpp.market.bidding;

import com.openvpp.common.enums.Scenario;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

/**
 * 分仓组合器 —— 多市场联合优化的组织载体（47241 VPP 单元的交易侧落地）。
 *
 * 同一个资源池按容量比例划入不同"仓位"分别申报不同市场：
 * 储能 3MW → 60% 现货套利仓 + 40% 调频仓；
 * 约束：各仓分配之和 ≤ 资源可信容量（物理上不能卖两次）。
 * 分仓而非物理拆分：同一储能站按容量比例分，不按设备台数拆。
 */
public class PortfolioAllocator {

    /**
     * 校验并归一化分仓方案。
     *
     * @param allocations 场景 → 拟分配容量
     * @param totalCredibleKw 资源可信容量上限
     * @return 归一化后的分配方案（总和 = totalCredibleKw 或原总和取小）
     */
    public Map<Scenario, BigDecimal> normalize(Map<Scenario, BigDecimal> allocations,
                                               BigDecimal totalCredibleKw) {
        BigDecimal sum = allocations.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(totalCredibleKw) <= 0) {
            return new EnumMap<>(allocations);
        }
        // 超配：按比例压缩到可信容量，防"一个资源卖两次"
        BigDecimal ratio = totalCredibleKw.divide(sum, 6, java.math.RoundingMode.DOWN);
        Map<Scenario, BigDecimal> normalized = new EnumMap<>(Scenario.class);
        allocations.forEach((scenario, kw) ->
                normalized.put(scenario, kw.multiply(ratio)));
        return normalized;
    }
}
