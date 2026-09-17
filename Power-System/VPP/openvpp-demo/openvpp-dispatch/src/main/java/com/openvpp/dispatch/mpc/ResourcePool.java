package com.openvpp.dispatch.mpc;

/**
 * 资源池接口 —— 滚动优化的可调资源供给侧。
 *
 * 教学简化：以接口形式存在，本工程只承载其可调容量查询契约；
 * 生产实现对应第 14 篇聚合引擎输出的可调容量池。
 */
public interface ResourcePool {

    /**
     * 某时段可承诺的最大调节功率（kW）。
     *
     * @param periodIndex 时段序号（0 为当前时段）
     * @return 该时段可调容量上限
     */
    double availableKw(int periodIndex);
}
