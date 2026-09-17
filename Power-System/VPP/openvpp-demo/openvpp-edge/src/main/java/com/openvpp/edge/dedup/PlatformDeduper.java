package com.openvpp.edge.dedup;

import com.openvpp.edge.cache.CachedPoint;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 平台侧去重器 —— 补传重放时的幂等闸口。
 * 键 = (deviceId, property, ts) 三元组，与边缘缓存的 dedupKey 同源。
 * 演示版内存实现；生产形态为 Redis SETNX 或数据库唯一约束，
 * 配合时序库的"同时间戳同键覆盖写"构成端到端幂等。
 */
public class PlatformDeduper {

    private final Map<String, Boolean> seen = new ConcurrentHashMap<>();

    /**
     * 过滤出首次到达的点；重复点直接丢弃。
     * 返回本次真正应该入库的子集。
     */
    public List<CachedPoint> filterNew(List<CachedPoint> batch) {
        return batch.stream()
                .filter(p -> seen.putIfAbsent(p.getDedupKey(), Boolean.TRUE) == null)
                .collect(Collectors.toList());
    }

    public int seenCount() {
        return seen.size();
    }
}
