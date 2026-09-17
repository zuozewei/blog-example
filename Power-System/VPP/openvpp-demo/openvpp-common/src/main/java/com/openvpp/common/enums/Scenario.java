package com.openvpp.common.enums;

/**
 * 应用场景 —— 对应 GB/T 44260 配置要求第 9-11 条与 47241 的市场参与能力。
 * 不同场景对资源类型、响应时间的要求不同，是资源池分组的业务源头。
 */
public enum Scenario {

    /** 调峰/需求响应：分钟级，调节容量宜不低于 1MW，以可调负荷为主 */
    PEAK_SHIFT,

    /** 调频：秒级～1min 内响应，宜配置负反馈控制，以储能/充电桩为主 */
    FREQ_REG,

    /** 备用：按需投入，以确定性调节容量资源为主 */
    RESERVE,

    /** 电能量市场：日前/日内/现货报量报价 */
    ENERGY_MARKET
}
