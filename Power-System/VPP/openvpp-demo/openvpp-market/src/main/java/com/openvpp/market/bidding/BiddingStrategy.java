package com.openvpp.market.bidding;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 竞价策略器 —— 日前报量报价的决策核心。
 *
 * 报量决策 = f(可信容量, 置信度, 风险偏好)：
 *   报量 = 可信容量 × 置信度 × 风险偏好系数
 * 风险约束两道：
 * 1. 报量不得超过聚合容量池（第 14 篇 InstructionDecomposer 的前置同源约束）；
 * 2. 预期偏差考核成本不得吞噬预期收益（报太高做不到，罚的比赚的多）。
 */
public class BiddingStrategy {

    /** 考核惩罚单价与收益单价的比值：超过此值报量必须保守 */
    private static final BigDecimal PENALTY_RATIO_WARNING = new BigDecimal("1.5");

    /**
     * 报量决策。
     *
     * @param credibleCapacityKw 聚合可信容量池
     * @param confidence         当前时段综合置信度（0-1）
     * @param appetite           风险偏好
     */
    public BigDecimal decideQuantity(BigDecimal credibleCapacityKw,
                                     BigDecimal confidence, RiskAppetite appetite) {
        BigDecimal quantity = credibleCapacityKw
                .multiply(confidence)
                .multiply(appetite.getQuantityFactor())
                .setScale(3, RoundingMode.DOWN);
        // 风险约束 1：不得超容量池
        return quantity.min(credibleCapacityKw);
    }

    /**
     * 报价风险校验：预期考核成本 vs 预期收益。
     * 返回 true = 风险可接受；false = 报量过于激进，应降风险偏好。
     *
     * @param quantityKwh    报量
     * @param expectedPrice  预期出清价
     * @param penaltyPrice   偏差考核单价
     * @param confidence     兑现概率（置信度）
     */
    public boolean riskAcceptable(BigDecimal quantityKwh, BigDecimal expectedPrice,
                                  BigDecimal penaltyPrice, BigDecimal confidence) {
        BigDecimal expectedRevenue = quantityKwh.multiply(expectedPrice).multiply(confidence);
        // 预期考核 = 报量 × (1-置信度) × 考核单价（兑现不足的部分按考核价罚）
        BigDecimal expectedPenalty = quantityKwh
                .multiply(BigDecimal.ONE.subtract(confidence))
                .multiply(penaltyPrice);
        return expectedRevenue.compareTo(expectedPenalty.multiply(PENALTY_RATIO_WARNING)) >= 0;
    }
}
