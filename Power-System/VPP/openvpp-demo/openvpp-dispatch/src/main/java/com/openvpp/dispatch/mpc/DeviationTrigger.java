package com.openvpp.dispatch.mpc;

/**
 * 偏差触发器 —— 滚动优化由「预测偏差超阈值」驱动，而非固定周期空跑（专栏第 33 篇）。
 *
 * 工程口径：阈值按偏差考核的免罚带反推（第 17 篇 ≥90% 免罚档），
 * 免罚带内的偏差重优化纯属浪费求解资源。
 *
 * 教学简化：只判定单点偏差是否超阈值，不处理多时段窗口滑动。
 */
public final class DeviationTrigger {

    private final double thresholdKw;

    /**
     * @param thresholdKw 触发阈值（kW），按免罚带反推：
     *                    例如免罚带 ±10%、申报容量 5MW，则阈值取 500kW
     */
    public DeviationTrigger(double thresholdKw) {
        if (thresholdKw < 0) {
            throw new IllegalArgumentException("触发阈值不能为负: " + thresholdKw);
        }
        this.thresholdKw = thresholdKw;
    }

    /**
     * 判定当前偏差是否触发滚动重优化。
     *
     * @param actualKw   实测功率（kW）
     * @param forecastKw 预测功率（kW）
     * @return true 表示 |actual - forecast| > threshold，应触发重优化
     */
    public boolean shouldTrigger(double actualKw, double forecastKw) {
        return Math.abs(actualKw - forecastKw) > thresholdKw;
    }

    public double thresholdKw() {
        return thresholdKw;
    }
}
