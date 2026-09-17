package com.openvpp.market.bidding;

import java.math.BigDecimal;

/**
 * 风险偏好档位 —— 竞价策略的胆量旋钮。
 * 第 12/13 篇的置信度在这里变现：同一个 P_50 预测，
 * 保守型按 P_90 报量（兑现概率高，量低），激进型按 P_50 报量（量高，赌兑现）。
 */
public enum RiskAppetite {

    /** 保守：按悲观分位报量，偏差考核风险最小，适合新入市/资质保级期 */
    CONSERVATIVE(new BigDecimal("0.70")),

    /** 均衡：按中位偏下报量，兼顾收益与风险 */
    BALANCED(new BigDecimal("0.85")),

    /** 激进：按中位报量，收益最大化，适合有储能兜底/置信度极高时 */
    AGGRESSIVE(new BigDecimal("1.00"));

    /** 报量系数：对 P_50 的折扣 */
    private final BigDecimal quantityFactor;

    RiskAppetite(BigDecimal quantityFactor) {
        this.quantityFactor = quantityFactor;
    }

    public BigDecimal getQuantityFactor() {
        return quantityFactor;
    }
}
