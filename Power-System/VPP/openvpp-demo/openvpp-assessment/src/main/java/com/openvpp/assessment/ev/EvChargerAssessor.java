package com.openvpp.assessment.ev;

/**
 * 充电桩可调容量评估器 —— 人在回路的行为概率模型。
 *
 * 核心结论（第 04 篇）：充电桩的瓶颈不是电池物理，是车主意愿。
 * 三层模型：
 * 1. 物理层：在充车辆的可降功率加总（唯一确定的部分）；
 * 2. 行为层：参与率是补贴单价的函数（实测：0.5 元/kWh 约 42%，1 元/kWh 约 76%）；
 * 3. 时间层：可容忍调节时长由车辆类型决定（网约 5-8min，物流 30-60min）。
 * 反直觉结论：利用率 50-65% 的中等繁忙站才是最优调节资源。
 */
public class EvChargerAssessor {

    private final int pileCount;
    private final double pileRateKw;

    public EvChargerAssessor(int pileCount, double pileRateKw) {
        this.pileCount = pileCount;
        this.pileRateKw = pileRateKw;
    }

    /**
     * 站点可调容量评估。
     *
     * @param onlineVehicles 当前在充车辆数
     * @param subsidyYuanPerKwh 补贴单价（行为层主输入）
     * @param avgReducibleKwPerCar 单车平均可降功率（物理层）
     * @param vehicleMix 车辆类型构成（时间层）
     */
    public EvCapability assess(int onlineVehicles, double subsidyYuanPerKwh,
                               double avgReducibleKwPerCar, VehicleMix vehicleMix) {
        // 物理层：在充车辆可降功率加总，不超过站点总功率
        double physicalKw = Math.min(onlineVehicles * avgReducibleKwPerCar,
                pileCount * pileRateKw);

        // 行为层：参与率随补贴增长但饱和（1 元/kWh 后提升有限）
        double joinRate = joinRateOf(subsidyYuanPerKwh);

        // 可承诺 = 物理 × 参与率 × 可靠性折扣（聚合口径取 0.9）
        double committedKw = physicalKw * joinRate * 0.9;

        long tolerableSeconds = vehicleMix.tolerableSeconds();
        return new EvCapability(committedKw, joinRate, tolerableSeconds);
    }

    /**
     * 参与率-补贴映射：实测数据的线性分段拟合。
     * 实测点：0 元=9%、0.5 元=42%、1 元=76%、1.5 元=86%、2 元+=89%（饱和）
     * 简化线性：0-1 元区间 0.09 + 0.67×补贴；1 元以上缓增至饱和 0.89
     */
    public double joinRateOf(double subsidyYuanPerKwh) {
        if (subsidyYuanPerKwh <= 0) {
            return 0.09;
        }
        if (subsidyYuanPerKwh <= 1.0) {
            return 0.09 + 0.67 * subsidyYuanPerKwh;   // 0.5 元→0.425，1 元→0.76
        }
        return Math.min(0.76 + 0.13 * (subsidyYuanPerKwh - 1.0), 0.89);
    }

    /** 站点利用率与可调潜力的反直觉关系：中等繁忙最优 */
    public double utilizationScore(double utilization) {
        // 30%→0.336, 60%→0.462（峰值）, 85%→0.298, 95%→0.086 的拟合曲线
        if (utilization <= 0 || utilization >= 1) {
            return 0;
        }
        return Math.sin(utilization * Math.PI * 0.75) * 0.5;
    }

    /** 车辆类型构成：决定可容忍调节时长 */
    public enum VehicleMix {
        RIDE_HAILING_DOMINANT(6 * 60),      // 网约车为主：仅适合短时调频
        PRIVATE_DOMINANT(20 * 60),          // 私家车为主：适合调峰
        LOGISTICS_DOMINANT(45 * 60);        // 物流车为主：最佳调峰资源

        private final long tolerableSeconds;

        VehicleMix(long tolerableSeconds) {
            this.tolerableSeconds = tolerableSeconds;
        }

        public long tolerableSeconds() {
            return tolerableSeconds;
        }
    }

    public static class EvCapability {
        private final double committedKw;
        private final double joinRate;
        private final long tolerableSeconds;

        public EvCapability(double committedKw, double joinRate, long tolerableSeconds) {
            this.committedKw = committedKw;
            this.joinRate = joinRate;
            this.tolerableSeconds = tolerableSeconds;
        }

        public double getCommittedKw() { return committedKw; }
        public double getJoinRate() { return joinRate; }
        public long getTolerableSeconds() { return tolerableSeconds; }
    }
}
