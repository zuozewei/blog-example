package com.openvpp.edge.cache;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 边缘缓存服务 —— 断网期间本地暂存遥测，恢复后按序补传。
 * 演示版用内存队列 + 容量上限；生产形态替换为 SQLite/RocksDB 落盘，
 * 接口与驱逐策略不变。
 *
 * 容量策略是边缘侧最重要的运维决策：缓存写满时丢弃最旧（遥测丢旧保新，
 * 因为旧数据对实时调度已无意义，而最新状态决定当前决策）。
 */
public class EdgeCacheService {

    private final int capacity;
    private final Deque<CachedPoint> queue = new ArrayDeque<>();

    public EdgeCacheService(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void cache(CachedPoint point) {
        while (queue.size() >= capacity) {
            queue.pollFirst();   // 丢最旧
        }
        queue.offerLast(point);
    }

    /** 取出一批待补传（不删除），发送成功后再 confirm */
    public synchronized List<CachedPoint> peekBatch(int max) {
        List<CachedPoint> batch = new ArrayList<>();
        for (CachedPoint p : queue) {
            if (batch.size() >= max) {
                break;
            }
            batch.add(p);
        }
        return batch;
    }

    /** 确认一批已送达，从缓存移除 */
    public synchronized void confirm(List<CachedPoint> batch) {
        queue.removeAll(batch);
    }

    public synchronized int size() {
        return queue.size();
    }
}
