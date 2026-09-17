package com.openvpp.iot.shadow;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 影子仓库 —— 按设备 ID 存取影子。
 * 演示版用内存 Map；生产形态替换为 Redis Hash
 * （key=shadow:{deviceId}，field=属性标识，附带 TTL 与持久化策略）。
 */
public class DeviceShadowRepository {

    private final Map<String, DeviceShadow> shadows = new ConcurrentHashMap<>();

    public DeviceShadow getOrCreate(String deviceId, String modelId) {
        return shadows.computeIfAbsent(deviceId, id -> new DeviceShadow(id, modelId));
    }

    public DeviceShadow get(String deviceId) {
        return shadows.get(deviceId);
    }

    public int size() {
        return shadows.size();
    }
}
