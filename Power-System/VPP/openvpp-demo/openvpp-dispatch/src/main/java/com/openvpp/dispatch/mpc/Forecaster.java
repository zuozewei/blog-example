package com.openvpp.dispatch.mpc;

import java.time.Instant;

/**
 * 预测器接口 —— MPC 三段式之「预测」段（专栏第 33 篇）。
 *
 * 生产形态为时间序列 + 卡尔曼滤波混合预测；本工程只定义契约，
 * 教学实现由测试内的固定序列桩替代。
 */
public interface Forecaster {

    /**
     * 给出从 now 起未来 periods 个时段的负荷/出力预测序列。
     *
     * @param now     当前时刻
     * @param periods 预测时窗内的时段数（滚动时域长度）
     * @return 预测序列（kW）
     */
    ForecastSeries forecast(Instant now, int periods);
}
