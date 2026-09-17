package com.openvpp.edge;

import com.openvpp.edge.cache.CachedPoint;
import com.openvpp.edge.cache.EdgeCacheService;
import com.openvpp.edge.dedup.PlatformDeduper;
import com.openvpp.edge.resend.ResendScheduler;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 边缘断网续传全场景单测：
 * 断网积压 → 恢复补传 → 失败保留 → 限速 → 平台幂等去重。
 */
class EdgeResendTest {

    @Test
    void 断网积压与恢复补传() {
        EdgeCacheService cache = new EdgeCacheService(1000);
        AtomicBoolean online = new AtomicBoolean(false);
        List<CachedPoint> platformInbox = new ArrayList<>();

        ResendScheduler scheduler = new ResendScheduler(cache, online::get,
                batch -> { platformInbox.addAll(batch); return true; },
                10, 5);

        // 断网期：50 条遥测全部落缓存，tick 不发
        for (int i = 0; i < 50; i++) {
            cache.cache(new CachedPoint("dev-001", "power", 1000L + i * 1000, 100.0 + i));
        }
        assertEquals(0, scheduler.tick(), "断网期不应发送");
        assertEquals(50, cache.size());

        // 恢复：两轮 tick 补完（每轮限速 5 批 × 10 条 = 50）
        online.set(true);
        int sent = scheduler.tick();
        assertEquals(50, sent);
        assertEquals(0, cache.size());
        assertEquals(50, platformInbox.size());
    }

    @Test
    void 发送失败保留缓存下轮重试() {
        EdgeCacheService cache = new EdgeCacheService(100);
        cache.cache(new CachedPoint("dev-002", "soc", 2000L, 62.0));

        AtomicBoolean sendOk = new AtomicBoolean(false);
        ResendScheduler scheduler = new ResendScheduler(cache, () -> true,
                batch -> sendOk.get(), 10, 3);

        assertEquals(0, scheduler.tick(), "发送失败不得确认");
        assertEquals(1, cache.size(), "失败的批次必须保留");

        sendOk.set(true);
        assertEquals(1, scheduler.tick(), "恢复后重试成功");
        assertEquals(0, cache.size());
    }

    @Test
    void 补传限速实时优先() {
        EdgeCacheService cache = new EdgeCacheService(1000);
        for (int i = 0; i < 100; i++) {
            cache.cache(new CachedPoint("dev-003", "power", 3000L + i * 1000, 50.0));
        }
        ResendScheduler scheduler = new ResendScheduler(cache, () -> true,
                batch -> true, 10, 2);   // 每轮最多 2 批

        int sent = scheduler.tick();
        assertEquals(20, sent, "单轮补传不得超限速配额");
        assertEquals(80, cache.size(), "剩余补传任务下轮继续");
    }

    @Test
    void 平台幂等去重重复补传不重复入库() {
        PlatformDeduper deduper = new PlatformDeduper();
        List<CachedPoint> batch = List.of(
                new CachedPoint("dev-004", "power", 4000L, 120.5),
                new CachedPoint("dev-004", "power", 5000L, 121.0));

        List<CachedPoint> first = deduper.filterNew(batch);
        List<CachedPoint> replay = deduper.filterNew(batch);   // 网络重放

        assertEquals(2, first.size());
        assertEquals(0, replay.size(), "重复补传必须被去重");
        assertEquals(2, deduper.seenCount());
    }

    @Test
    void 缓存写满丢最旧保最新() {
        EdgeCacheService cache = new EdgeCacheService(3);
        for (int i = 0; i < 5; i++) {
            cache.cache(new CachedPoint("dev-005", "power", 6000L + i, i));
        }
        List<CachedPoint> remaining = cache.peekBatch(10);
        assertEquals(3, remaining.size());
        assertEquals(6002L, remaining.get(0).getTsMs(), "最旧的应被驱逐");
    }
}
