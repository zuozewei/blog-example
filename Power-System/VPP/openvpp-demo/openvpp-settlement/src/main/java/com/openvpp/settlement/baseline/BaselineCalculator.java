package com.openvpp.settlement.baseline;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基线核算器 —— 需求响应结算的参照系（"反事实估算"）。
 *
 * 口径：相似日平均法（结算主流口径，规则透明可复核）。
 * 样本仅限响应日之前的历史数据；响应期实际负荷不进入基线计算——
 * 否则响应越成功、基线被压得越低、响应量被反噬（专栏第 17 篇反例）。
 *
 * 三道防线防"游戏基线"：
 * 1. 响应日当天及之后的数据（含管道故障混入的未来日期）不得入池；
 * 2. 基线锁定：用响应日前数据计算并锁定，锁定后新数据不得修改（数据管道层职责）；
 * 3. 高异常剔除：高出池内中位数 rule.highOutlierFactor 倍的样本日剔除，
 *    剔除依据只来自历史数据自身，不依赖响应期任何数据。
 */
public class BaselineCalculator {

    private final BaselineRule rule;

    public BaselineCalculator(BaselineRule rule) {
        this.rule = rule;
    }

    /**
     * 计算某结算时段的基线负荷（kW）。
     *
     * @param samples     候选相似日样本（上游已按日类型/节假日口径预筛选）
     * @param responseDay 响应日
     * @throws IllegalStateException 有效样本不足时拒绝核算（宁可不算，不可算错）
     */
    public double calculate(List<SamplePoint> samples, LocalDate responseDay) {
        List<Double> pool = new ArrayList<>();
        for (SamplePoint s : samples) {
            // 防线 1：响应日当天起的数据一律不入池（含未来日期混入）
            if (!s.date().isBefore(responseDay)) {
                continue;
            }
            // 缺失点跳过；有效零值是真实计量，保留
            if (s.loadKw() == null) {
                continue;
            }
            pool.add(s.loadKw());
        }
        if (pool.size() < rule.minValidDays()) {
            throw new IllegalStateException(
                    "有效相似日样本不足 " + rule.minValidDays() + " 天，拒绝核算基线");
        }

        // 防线 3：高异常剔除——依据仅来自历史数据自身的中位数
        double median = median(pool);
        double ceiling = median * rule.highOutlierFactor();
        List<Double> cleaned = pool.stream().filter(v -> v <= ceiling).collect(Collectors.toList());
        if (cleaned.size() < rule.minValidDays()) {
            throw new IllegalStateException("异常剔除后有效样本不足，转人工核查");
        }
        return cleaned.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
    }

    private static double median(List<Double> values) {
        List<Double> sorted = values.stream().sorted().collect(Collectors.toList());
        int n = sorted.size();
        return n % 2 == 1
                ? sorted.get(n / 2)
                : (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
    }
}
