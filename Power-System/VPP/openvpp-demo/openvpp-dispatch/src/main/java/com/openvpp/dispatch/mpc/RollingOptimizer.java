package com.openvpp.dispatch.mpc;

/**
 * 滚动时域优化器接口 —— MPC 三段式之「滚动优化」段（专栏第 33 篇）。
 *
 * 求解 N 时段最优计划，但只执行第 1 时段；时窗前移后重新求解。
 */
public interface RollingOptimizer {

    /**
     * 在给定市场边界内求解未来 N 时段最优计划。
     *
     * @param pool           可调资源池
     * @param series         校正后的预测序列
     * @param marketBoundary 市场申报边界（kW），计划不得随意突破——
     *                       突破申报曲线要付偏差考核的钱（专栏第 17 篇）
     * @return N 时段最优计划（取第 1 时段执行）
     */
    DispatchPlan optimize(ResourcePool pool, ForecastSeries series, double marketBoundary);
}
