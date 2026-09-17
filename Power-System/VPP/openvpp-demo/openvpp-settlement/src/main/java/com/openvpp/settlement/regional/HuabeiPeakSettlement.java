package com.openvpp.settlement.regional;

/**
 * 华北调峰单时段结算 —— 专栏第 34 篇「双重 min 非对称激励」公式直译。
 *
 * 公式：Rt = K · min{P/Pz, 1} · min{P, Pz} · tC · CC
 *
 * 非对称激励结构：
 * <ul>
 *   <li>响应不足（P &lt; Pz）→ 性能系数 P/Pz 打折 + 电量基数降到 P → 惩罚叠加（双重）</li>
 *   <li>超额响应（P &gt; Pz）→ 性能系数封顶 1 + 电量基数封顶 Pz → 多调白送（零奖励）</li>
 * </ul>
 *
 * 悬崖条款：偏差超过 30% 的时段全部不结算——先判悬崖，再套公式，顺序不能反。
 *
 * 工程注意：K（省网火电平均负荷率倒数）逐期更新，必须外部数据接入而非配置常量；
 * Pz 为 0 的时段短路返回，防除零。
 *
 * 教学简化：只做单时段结算，不处理跨时段累计与多资源分摊。
 */
public final class HuabeiPeakSettlement {

    /** 出清时间间隔：15 分钟 = 0.25h（华北调峰口径） */
    public static final double INTERVAL_HOURS = 0.25;

    /** 悬崖阈值：偏差超过 30% 全时段归零 */
    public static final double CLIFF_DEVIATION_PCT = 30.0;

    /**
     * 单时段结算。
     *
     * @param actualKw      实际充电功率 P（kW，实测）
     * @param bidKw         中标容量 Pz（kW，申报承诺）
     * @param kFactor       K：省网火电平均负荷率的倒数（逐期外部接入）
     * @param clearingPrice 边际出清价格 CC（元/kWh）
     * @return 该时段结算金额（元）；悬崖或除零保护时返回 0
     */
    public static double settle(double actualKw, double bidKw,
                                double kFactor, double clearingPrice) {
        // 除零保护：Pz 为 0 的时段短路返回
        if (bidKw <= 0) {
            return 0.0;
        }
        // 悬崖判定在前：偏差超 30% 全时段归零，不进入公式计算
        double deviationPct = Math.abs(actualKw - bidKw) / bidKw * 100.0;
        if (deviationPct > CLIFF_DEVIATION_PCT) {
            return 0.0;
        }
        // 双重 min：性能系数封顶 1，电量基数取较小值
        double performance = Math.min(actualKw / bidKw, 1.0);
        double energyBase = Math.min(actualKw, bidKw);
        return kFactor * performance * energyBase * INTERVAL_HOURS * clearingPrice;
    }
}
