package com.openvpp.edge.cache;

import java.util.Map;

/**
 * 边缘缓存条目 —— 断网期间落盘的遥测数据。
 * 键设计即幂等设计：(deviceId, property, ts) 三元组唯一标识一条遥测，
 * 补传重放时平台按同键去重，天然幂等。
 */
public class CachedPoint {

    private final String deviceId;
    private final String property;
    private final long tsMs;
    private final double value;

    /** 平台侧去重键 */
    private final String dedupKey;

    public CachedPoint(String deviceId, String property, long tsMs, double value) {
        this.deviceId = deviceId;
        this.property = property;
        this.tsMs = tsMs;
        this.value = value;
        this.dedupKey = deviceId + "|" + property + "|" + tsMs;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getProperty() {
        return property;
    }

    public long getTsMs() {
        return tsMs;
    }

    public double getValue() {
        return value;
    }

    public String getDedupKey() {
        return dedupKey;
    }
}
