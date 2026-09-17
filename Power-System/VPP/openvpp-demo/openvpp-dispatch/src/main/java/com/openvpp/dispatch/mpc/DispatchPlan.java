package com.openvpp.dispatch.mpc;

/**
 * 滚动优化求解结果 —— N 时段最优计划。
 *
 * MPC 核心动作：求解未来 N 个时段的最优计划，但<b>只执行第 1 个时段</b>的指令，
 * 其余时段在下一轮滚动中被重新求解（专栏第 33 篇）。
 */
public final class DispatchPlan {

    private final double[] scheduleKw;

    public DispatchPlan(double[] scheduleKw) {
        if (scheduleKw == null || scheduleKw.length == 0) {
            throw new IllegalArgumentException("计划序列不能为空");
        }
        this.scheduleKw = new double[scheduleKw.length];
        System.arraycopy(scheduleKw, 0, this.scheduleKw, 0, scheduleKw.length);
    }

    public int periods() {
        return scheduleKw.length;
    }

    public double valueAt(int index) {
        return scheduleKw[index];
    }

    /** 只取第 1 时段执行——滚动时域优化的执行纪律 */
    public DispatchPlan firstPeriodOnly() {
        return new DispatchPlan(new double[]{scheduleKw[0]});
    }
}
