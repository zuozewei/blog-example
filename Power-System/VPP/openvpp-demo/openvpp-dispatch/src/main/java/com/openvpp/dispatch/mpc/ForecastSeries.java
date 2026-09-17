package com.openvpp.dispatch.mpc;

import java.util.Arrays;

/**
 * 预测序列 —— 未来 N 个时段的负荷/出力预测值（kW）。
 *
 * 教学简化：只承载等间隔时段的功率值，不携带时间戳与置信区间。
 */
public final class ForecastSeries {

    private final double[] valuesKw;

    public ForecastSeries(double[] valuesKw) {
        if (valuesKw == null || valuesKw.length == 0) {
            throw new IllegalArgumentException("预测序列不能为空");
        }
        this.valuesKw = Arrays.copyOf(valuesKw, valuesKw.length);
    }

    public int size() {
        return valuesKw.length;
    }

    public double valueAt(int index) {
        return valuesKw[index];
    }

    public double[] valuesKw() {
        return Arrays.copyOf(valuesKw, valuesKw.length);
    }

    /** 反馈校正：整体平移预测偏置，返回校正后的新序列（原序列不可变） */
    public ForecastSeries shift(double biasKw) {
        double[] shifted = new double[valuesKw.length];
        for (int i = 0; i < valuesKw.length; i++) {
            shifted[i] = valuesKw[i] + biasKw;
        }
        return new ForecastSeries(shifted);
    }
}
