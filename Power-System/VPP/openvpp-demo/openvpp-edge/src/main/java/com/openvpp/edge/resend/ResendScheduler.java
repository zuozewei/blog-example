package com.openvpp.edge.resend;

import com.openvpp.edge.cache.CachedPoint;
import com.openvpp.edge.cache.EdgeCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;

/**
 * 补传调度器 —— 网络状态感知的发送开关。
 *
 * 设计要点：
 * 1. 网络恢复不是"收到第一条成功发送"，而是显式的连通性判定（ping/心跳），
 *    避免"半通"状态下边发边丢；
 * 2. 补传与实时数据同通道发送，实时优先——补传按批次限速，
 *    防止补传风暴挤占实时遥测带宽；
 * 3. 发送失败保留在缓存，下个周期重试，配合平台幂等去重构成
 *    at-least-once 语义（VPP 遥测宁重勿丢：重复可去重，丢失无法结算）。
 */
public class ResendScheduler {

    private static final Logger log = LoggerFactory.getLogger(ResendScheduler.class);

    /** 网络探测器：true = 平台可达 */
    @FunctionalInterface
    public interface ConnectivityProbe {
        boolean reachable();
    }

    private final EdgeCacheService cache;
    private final ConnectivityProbe probe;
    /** 上行发送函数：返回 true 表示平台确认接收 */
    private final Function<List<CachedPoint>, Boolean> sender;
    private final int batchSize;
    /** 补传限速：每轮最多补传批数，实时数据优先 */
    private final int maxBatchesPerRound;

    public ResendScheduler(EdgeCacheService cache, ConnectivityProbe probe,
                           Function<List<CachedPoint>, Boolean> sender,
                           int batchSize, int maxBatchesPerRound) {
        this.cache = cache;
        this.probe = probe;
        this.sender = sender;
        this.batchSize = batchSize;
        this.maxBatchesPerRound = maxBatchesPerRound;
    }

    /**
     * 调度主循环的一步：检测连通 → 按批补传 → 失败即停。
     * 由边缘侧定时任务驱动（演示版单测手动调用）。
     */
    public int tick() {
        if (cache.size() == 0) {
            return 0;
        }
        if (!probe.reachable()) {
            log.info("平台不可达，缓存继续积压: {} 条", cache.size());
            return 0;
        }
        int sent = 0;
        for (int i = 0; i < maxBatchesPerRound && cache.size() > 0; i++) {
            List<CachedPoint> batch = cache.peekBatch(batchSize);
            Boolean ok = sender.apply(batch);
            if (Boolean.TRUE.equals(ok)) {
                cache.confirm(batch);
                sent += batch.size();
            } else {
                log.warn("补传批次发送失败，{} 条保留待重试", batch.size());
                break;   // 失败即停：通道异常时不再消耗配额
            }
        }
        return sent;
    }
}
