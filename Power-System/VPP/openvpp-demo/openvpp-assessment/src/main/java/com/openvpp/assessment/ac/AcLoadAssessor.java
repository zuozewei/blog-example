package com.openvpp.assessment.ac;

/**
 * 空调负荷可调容量评估器 —— ETP（等效热参数）模型。
 *
 * 核心物理（第 04 篇）：建筑热惯性是可调潜力的来源——
 * 关停后温度缓慢漂移的窗口期，就是可承诺的响应时长。
 * 三个工程细节必须进模型：恢复期、回弹系数、预冷增益。
 */
public class AcLoadAssessor {

    private final double ratedCoolingKw;
    private final double thermalCapKjPerDeg;
    private final double maxTempRiseDeg;

    /** 回弹缓冲系数：承诺容量打折，防止多栋同时恢复顶穿台变 */
    private static final double REBOUND_FACTOR = 0.9;
    /** 预冷增益：响应前预冷 2°C，时长近似翻倍 */
    private static final double PRECOOL_GAIN_DEG = 2.0;

    public AcLoadAssessor(double ratedCoolingKw, double thermalCapKjPerDeg, double maxTempRiseDeg) {
        this.ratedCoolingKw = ratedCoolingKw;
        this.thermalCapKjPerDeg = thermalCapKjPerDeg;
        this.maxTempRiseDeg = maxTempRiseDeg;
    }

    /**
     * 完全关停模式评估。
     *
     * @param netHeatGainKw 关停期间净得热（内部热源+太阳辐射-围护散失）
     * @return 可削减功率、可持续时长、恢复期
     */
    public AcCapability assessFullShutdown(double netHeatGainKw, double baseLoadKw) {
        // ETP 核心公式：t = C × ΔT / Q_net
        long endureSeconds = (long) (thermalCapKjPerDeg * maxTempRiseDeg / netHeatGainKw);

        // 恢复期：满载拉回温度，经验为 endure 的 1/3 ~ 1/2
        long recoverySeconds = (long) (endureSeconds * 0.4);

        // 可承诺削减 = 基准功率 × 回弹系数
        double committedShedKw = baseLoadKw * REBOUND_FACTOR;

        return new AcCapability(committedShedKw, endureSeconds, recoverySeconds);
    }

    /** 预冷模式：响应前降温 2°C，可容忍温升扩大，时长近似翻倍 */
    public AcCapability assessWithPrecool(double netHeatGainKw, double baseLoadKw) {
        long endureSeconds = (long) (thermalCapKjPerDeg * (maxTempRiseDeg + PRECOOL_GAIN_DEG) / netHeatGainKw);
        long recoverySeconds = (long) (endureSeconds * 0.4);
        return new AcCapability(baseLoadKw * REBOUND_FACTOR, endureSeconds, recoverySeconds);
    }

    /** 轮停周期合法性校验：必须大于压缩机重启保护延时 */
    public boolean rotationCycleValid(int cycleSeconds, int restartGuardSeconds) {
        return cycleSeconds > restartGuardSeconds;
    }

    public static class AcCapability {
        private final double shedKw;
        private final long endureSeconds;
        private final long recoverySeconds;

        public AcCapability(double shedKw, long endureSeconds, long recoverySeconds) {
            this.shedKw = shedKw;
            this.endureSeconds = endureSeconds;
            this.recoverySeconds = recoverySeconds;
        }

        public double getShedKw() { return shedKw; }
        public long getEndureSeconds() { return endureSeconds; }
        public long getRecoverySeconds() { return recoverySeconds; }
        /** 完整周期 = 响应 + 恢复，调度排程用这个 */
        public long fullCycleSeconds() { return endureSeconds + recoverySeconds; }
    }
}
