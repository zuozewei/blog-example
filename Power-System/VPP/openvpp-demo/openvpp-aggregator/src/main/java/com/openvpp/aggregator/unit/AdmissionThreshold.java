package com.openvpp.aggregator.unit;

/**
 * 准入门槛常量 —— 对应 GB/T 47241-2026 第 4.2 条。
 * 条款为"宜"（推荐性），本身不构成强制验收依据；
 * 但地方细则与市场规则大概率引用该组数值，工程上视为事实准入基准。
 */
public final class AdmissionThreshold {

    /** 总聚合容量 10MW */
    public static final long MIN_TOTAL_AGGREGATE_KW = 10_000;

    /** 总调节容量 5MW */
    public static final long MIN_TOTAL_ADJUST_KW = 5_000;

    /** 单元调节容量 1MW，对应 44260"调峰调节容量宜≥1MW" */
    public static final long MIN_UNIT_ADJUST_KW = 1_000;

    /** 调节速率 %/min：1MW 单元每分钟至少变化 30kW */
    public static final double MIN_RAMP_PERCENT = 3.0;

    /** 持续调节时间 1h */
    public static final long MIN_SUSTAIN_HOURS = 1;

    private AdmissionThreshold() {
    }
}
