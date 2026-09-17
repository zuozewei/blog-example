package com.openvpp.iot.shadow;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备影子 —— 设备最新状态的云端缓存。
 * 设备离线时平台照常可读状态；控制指令先写期望值（desired），
 * 设备上线后拉取差异执行并回执（reported 追平 desired 即闭环）。
 * 影子版本号用于并发合并：旧报文不得覆盖新状态。
 */
public class DeviceShadow {

    private final String deviceId;
    private final String modelId;

    /** 上报态：设备最新真实状态 */
    private final Map<String, Object> reported = new ConcurrentHashMap<>();

    /** 期望态：平台希望设备达到的状态（控制目标） */
    private final Map<String, Object> desired = new ConcurrentHashMap<>();

    private volatile long version;
    private volatile LocalDateTime lastReportTime;

    public DeviceShadow(String deviceId, String modelId) {
        this.deviceId = deviceId;
        this.modelId = modelId;
    }

    public void mergeReported(Map<String, Object> delta, long messageSeq) {
        // 版本校核：乱序到达的旧报文直接丢弃（44260 数据校核的落地）
        if (messageSeq < version) {
            return;
        }
        reported.putAll(delta);
        this.version = messageSeq;
        this.lastReportTime = LocalDateTime.now();
    }

    public void mergeDesired(Map<String, Object> delta) {
        desired.putAll(delta);
    }

    /** 待下发差异：期望值与上报态不一致的点 */
    public Map<String, Object> pendingDiff() {
        Map<String, Object> diff = new ConcurrentHashMap<>();
        desired.forEach((key, want) -> {
            Object actual = reported.get(key);
            if (actual == null || !actual.equals(want)) {
                diff.put(key, want);
            }
        });
        return diff;
    }

    public String getDeviceId() { return deviceId; }
    public String getModelId() { return modelId; }
    public Map<String, Object> getReported() { return reported; }
    public Map<String, Object> getDesired() { return desired; }
    public long getVersion() { return version; }
    public LocalDateTime getLastReportTime() { return lastReportTime; }
}
