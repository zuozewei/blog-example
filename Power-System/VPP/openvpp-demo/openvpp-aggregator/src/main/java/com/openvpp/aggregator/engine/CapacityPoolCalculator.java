package com.openvpp.aggregator.engine;

import java.math.BigDecimal;
import java.util.List;

/**
 * 容量池计算器 —— 聚合承诺容量的口径核心。
 * 聚合边界 ≠ 单资源容量代数和（第 04 篇空调篇的非线性教训）：
 * 承诺边界 = Σ(可信容量) × 聚合折扣
 * 可信容量 = 可调容量 × 置信度（单资源评估输出）
 * 聚合折扣 = 多样性/通信覆盖率/回弹缓冲的乘积（运营参数，非代码常量）
 */
public class CapacityPoolCalculator {

    /** 聚合折扣：回弹缓冲 × 通信覆盖，素材口径 0.9（空调篇聚合承诺公式） */
    private final double poolDiscount;

    public CapacityPoolCalculator(double poolDiscount) {
        this.poolDiscount = poolDiscount;
    }

    /**
     * 单元可信容量池：成员可信容量求和后打聚合折扣。
     */
    public BigDecimal poolOf(List<AssessedResource> members) {
        BigDecimal credibleSum = members.stream()
                .map(AssessedResource::credibleCapacityKw)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return credibleSum.multiply(BigDecimal.valueOf(poolDiscount));
    }

    /** 聚合总容量（全资源池 47241 总调节容量 5MW 准入线的数据源） */
    public BigDecimal totalOf(List<AssessedResource> pool) {
        return pool.stream()
                .map(AssessedResource::credibleCapacityKw)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public double getPoolDiscount() {
        return poolDiscount;
    }
}
