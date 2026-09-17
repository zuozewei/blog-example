package com.openvpp.iot.timeseries;

import java.util.List;

/**
 * 时序写入器 —— 存储引擎无关的读写契约。
 * TDengine / ClickHouse 各给一个实现，业务侧只面向此接口：
 * 第 8 篇的选型结论若变化，替换实现即可，上层零改动。
 */
public interface TsdbWriter {

    /** 初始化存储结构（库/超级表/MergeTree 表），幂等 */
    void initSchema();

    /** 批量写入遥测点 */
    void writeBatch(List<TelemetryPoint> points);

    /** 按设备+属性+时间窗查询，返回按时间升序的点列 */
    List<TelemetryPoint> queryRange(String deviceId, String property, long fromMs, long toMs);

    /** 实现标识：TDENGINE / CLICKHOUSE */
    String name();
}
