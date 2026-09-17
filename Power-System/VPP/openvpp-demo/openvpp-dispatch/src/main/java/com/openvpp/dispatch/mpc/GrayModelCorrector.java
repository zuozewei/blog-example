package com.openvpp.dispatch.mpc;

/**
 * 灰色模型误差外推校正器 —— 专栏第 33 篇反馈校正段的教学实现。
 *
 * 工程口径：快速滚动灰色模型的价值在「小样本」——只需最近几个时段的
 * 误差序列即可外推下一时段误差，不需要重新训练。
 *
 * 教学简化：以最近 window 个时段的算术平均误差作为偏置估计
 * （GM(1,1) 的一次累加在小样本下与滑动平均同量级），
 * 样本不足 window 时退化为全量平均；空样本返回 0（无修正）。
 */
public final class GrayModelCorrector implements ErrorCorrector {

    private final int window;

    /** @param window 误差窗口长度（时段数），工程典型值 4~8 */
    public GrayModelCorrector(int window) {
        if (window <= 0) {
            throw new IllegalArgumentException("误差窗口必须为正数");
        }
        this.window = window;
    }

    @Override
    public double estimateBias(double[] lastForecast, double[] lastActual) {
        if (lastForecast == null || lastActual == null || lastForecast.length != lastActual.length) {
            throw new IllegalArgumentException("预测与实测序列必须等长且非空");
        }
        if (lastActual.length == 0) {
            return 0.0;
        }
        int n = Math.min(window, lastActual.length);
        double sum = 0.0;
        for (int i = lastActual.length - n; i < lastActual.length; i++) {
            sum += lastActual[i] - lastForecast[i];
        }
        return sum / n;
    }
}
