package com.openvpp.iot.auth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 已用随机数登记簿 —— 防重放的唯一性底座（第 ③ 层）。
 *
 * 职责边界（这是本篇最容易讲错的一点）：
 *  - 时间窗口只限制消息**时效**——出窗的旧令牌失效；
 *  - 窗口**之内**的时间戳全部合法，攻击者在窗口内原样重发截获令牌，
 *    签名、时标检查全部通过——时间窗口对窗口内重放不设防；
 *  - 真正的防重放 = 每条认证消息携带一次性 nonce + 服务端登记已用 nonce，
 *    窗口内照样逐条查重。
 *
 * 演示版内存表 + 惰性过期清理（登记时顺手清掉出窗条目，无后台线程），
 * 单实例语义。生产形态：Redis SET NX PX（原子占位 + TTL 随窗口到期自动清除）
 * 或 DB 唯一约束；多实例部署时必须集中存储，否则重放打到另一台节点即穿透。
 */
public class NonceRegistry {

    /** 已用 nonce → 过期时刻（毫秒） */
    private final Map<String, Long> usedNonces = new ConcurrentHashMap<>();
    private final long windowMs;

    public NonceRegistry(long windowMs) {
        this.windowMs = windowMs;
    }

    /**
     * 登记 nonce：首次出现返回 true 并占位；已出现过（重放）返回 false。
     * 原子语义靠 ConcurrentHashMap.putIfAbsent 保证——并发重放下只有一个线程占位成功。
     */
    public boolean registerOnce(String deviceId, String nonce, long nowMs) {
        evictExpired(nowMs);
        String key = deviceId + "|" + nonce;
        return usedNonces.putIfAbsent(key, nowMs + windowMs) == null;
    }

    private void evictExpired(long nowMs) {
        usedNonces.values().removeIf(expireAt -> expireAt <= nowMs);
    }

    /** 当前登记量（测试与容量观测用） */
    public int size() {
        return usedNonces.size();
    }
}
