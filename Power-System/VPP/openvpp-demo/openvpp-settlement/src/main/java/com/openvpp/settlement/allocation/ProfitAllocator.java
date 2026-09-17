package com.openvpp.settlement.allocation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 收益分摊器 —— 平台收益分配到聚合的各用户。
 * 主流模式是"保底 + 分成"（第 01 篇术语表）：
 * 1. 保底：按各用户申报容量占比分配保底收益（旱涝保收，降低参与门槛）；
 * 2. 分成：超出保底的部分按实际贡献（各资源实际响应量占比）分配；
 * 3. 平台抽成：从分成部分按比例计提运营服务费。
 * 守恒底线：分配总额 = 平台实收，分毫不差（残差归并最大贡献者）。
 */
public class ProfitAllocator {

    /** 平台抽成比例（分成部分） */
    private final BigDecimal platformCutRate;

    public ProfitAllocator(BigDecimal platformCutRate) {
        this.platformCutRate = platformCutRate;
    }

    /**
     * 分摊一次 DR 收益。
     *
     * @param totalSettleYuan 平台实收结算金额（第 19 篇 DR 结算产出）
     * @param guaranteedYuan  保底总额（按容量占比分）
     * @param declaredKw      用户 → 申报容量
     * @param actualKwh       用户 → 实际响应量（贡献口径）
     * @return 用户 → 应分金额，总和守恒
     */
    public Map<String, BigDecimal> allocate(BigDecimal totalSettleYuan, BigDecimal guaranteedYuan,
                                            Map<String, BigDecimal> declaredKw,
                                            Map<String, BigDecimal> actualKwh) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();

        // 第一层：保底按申报容量占比
        BigDecimal totalDeclared = declaredKw.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, BigDecimal> guaranteedShare = new LinkedHashMap<>();
        declaredKw.forEach((user, kw) -> guaranteedShare.put(user,
                guaranteedYuan.multiply(kw).divide(totalDeclared, 2, RoundingMode.DOWN)));

        // 第二层：超额分成按实际贡献占比，平台先抽成
        BigDecimal surplus = totalSettleYuan.subtract(guaranteedYuan).max(BigDecimal.ZERO);
        BigDecimal distributable = surplus.multiply(BigDecimal.ONE.subtract(platformCutRate));
        BigDecimal totalActual = actualKwh.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal allocated = BigDecimal.ZERO;
        String topContributor = null;
        BigDecimal topContribution = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> e : actualKwh.entrySet()) {
            BigDecimal share = totalActual.signum() > 0
                    ? distributable.multiply(e.getValue()).divide(totalActual, 2, RoundingMode.DOWN)
                    : BigDecimal.ZERO;
            BigDecimal total = guaranteedShare.getOrDefault(e.getKey(), BigDecimal.ZERO).add(share);
            result.put(e.getKey(), total);
            allocated = allocated.add(total);
            if (e.getValue().compareTo(topContribution) > 0) {
                topContribution = e.getValue();
                topContributor = e.getKey();
            }
        }

        // 守恒：舍入残差归并最大贡献者
        BigDecimal residue = guaranteedYuan.add(distributable).subtract(allocated);
        if (residue.signum() != 0 && topContributor != null) {
            result.merge(topContributor, residue, BigDecimal::add);
        }
        return result;
    }
}
