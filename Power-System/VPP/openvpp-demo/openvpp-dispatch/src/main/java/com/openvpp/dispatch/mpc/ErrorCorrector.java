package com.openvpp.dispatch.mpc;

/**
 * 反馈校正器接口 —— MPC 三段式之「反馈校正」段（专栏第 33 篇）。
 *
 * 每轮把实测值与预测值的偏差喂回来，估计下一轮预测的整体偏置，
 * 误差不在滚动中累积。
 */
public interface ErrorCorrector {

    /**
     * 由上一轮「预测 vs 实测」估计预测偏置。
     *
     * @param lastForecast 上一轮预测序列（kW）
     * @param lastActual   上一轮实测序列（kW），与预测等长
     * @return 偏置估计（kW），正号表示实测系统性高于预测
     */
    double estimateBias(double[] lastForecast, double[] lastActual);
}
