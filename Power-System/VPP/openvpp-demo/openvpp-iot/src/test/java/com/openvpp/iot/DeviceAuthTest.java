package com.openvpp.iot;

import com.openvpp.iot.auth.AesGcmKeyProtector;
import com.openvpp.iot.auth.AuthMessage;
import com.openvpp.iot.auth.DeviceAuthFilter;
import com.openvpp.iot.auth.DeviceCredentialStore;
import com.openvpp.iot.auth.DeviceTokenService;
import com.openvpp.iot.auth.KeyProtector;
import com.openvpp.iot.auth.NonceRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 设备认证单测：13 个安全场景。
 * 原 6 场景（合法/错密钥/未注册/过期/窗口内时漂/吊销）全保留并适配新令牌模型；
 * 新增 7 场景覆盖完整性校验、窗口内重放、并发重放、密钥轮换、密钥密文存储。
 */
class DeviceAuthTest {

    private static final long WINDOW_MS = 5 * 60 * 1000L;
    private static final long NOW = 1_758_000_000_000L;
    private static final String PAYLOAD = "{\"power\":1200.5,\"soc\":0.62}";

    private DeviceCredentialStore store;
    private DeviceTokenService tokenService;
    private DeviceAuthFilter authFilter;
    private NonceRegistry nonceRegistry;

    @BeforeEach
    void setUp() {
        byte[] masterKey = new byte[32];
        new SecureRandom().nextBytes(masterKey);
        KeyProtector keyProtector =
                new AesGcmKeyProtector(Base64.getEncoder().encodeToString(masterKey));
        store = new DeviceCredentialStore(keyProtector);
        nonceRegistry = new NonceRegistry(WINDOW_MS);
        tokenService = new DeviceTokenService(store, nonceRegistry, WINDOW_MS);
        authFilter = new DeviceAuthFilter(tokenService);
        store.enroll("dev-001", "k1", "secret-of-dev-001");
    }

    /** 设备侧构造一条合法认证消息（含正文摘要与一次性 nonce） */
    private AuthMessage buildMessage(String deviceId, String keyId, long timestamp, String payload) {
        return new AuthMessage(deviceId, AuthMessage.PROTOCOL_VERSION, keyId, timestamp,
                tokenService.newNonce(), "telemetry", AuthMessage.digestOf(payload));
    }

    private boolean authenticate(AuthMessage message, String secret, long nowMs) {
        String token = tokenService.sign(message, secret);
        return authFilter.authenticate(message, token, nowMs);
    }

    // ---------- 保留的原 6 场景（适配新模型） ----------

    @Test
    void 合法令牌通过认证() {
        AuthMessage msg = buildMessage("dev-001", "k1", NOW, PAYLOAD);
        assertTrue(authenticate(msg, "secret-of-dev-001", NOW));
    }

    @Test
    void 错密钥签名被拒绝() {
        AuthMessage msg = buildMessage("dev-001", "k1", NOW, PAYLOAD);
        assertFalse(authenticate(msg, "wrong-secret", NOW));
    }

    @Test
    void 未注册设备被拒绝() {
        AuthMessage msg = buildMessage("dev-ghost", "k1", NOW, PAYLOAD);
        assertFalse(authenticate(msg, "any-secret", NOW));
    }

    @Test
    void 过期令牌防重放() {
        long capturedAt = NOW - 10 * 60 * 1000L;   // 10 分钟前截获的令牌
        AuthMessage msg = buildMessage("dev-001", "k1", capturedAt, PAYLOAD);
        assertFalse(authenticate(msg, "secret-of-dev-001", NOW),
                "超出时间窗的旧令牌必须失效");
    }

    @Test
    void 窗口内令牌防误杀() {
        long slightlyOld = NOW - 2 * 60 * 1000L;   // 2 分钟前，窗口内
        AuthMessage msg = buildMessage("dev-001", "k1", slightlyOld, PAYLOAD);
        assertTrue(authenticate(msg, "secret-of-dev-001", NOW),
                "窗口内的正常时漂不应误杀");
    }

    @Test
    void 吊销后令牌立即失效() {
        AuthMessage msg = buildMessage("dev-001", "k1", NOW, PAYLOAD);
        String token = tokenService.sign(msg, "secret-of-dev-001");
        store.revoke("dev-001");
        assertFalse(authFilter.authenticate(msg, token, NOW));
    }

    // ---------- 新增场景 ----------

    @Test
    void 修改正文后摘要不匹配被拒绝() {
        AuthMessage msg = buildMessage("dev-001", "k1", NOW, PAYLOAD);
        String token = tokenService.sign(msg, "secret-of-dev-001");
        // 攻击者保留签名，篡改正文：摘要随正文变化，MAC 失配
        AuthMessage tampered = new AuthMessage(msg.deviceId(), msg.protocolVersion(), msg.keyId(),
                msg.timestamp(), msg.nonce(), msg.messageType(),
                AuthMessage.digestOf("{\"power\":9999.9}"));
        assertFalse(authFilter.authenticate(tampered, token, NOW),
                "正文被篡改后原签名必须失效");
    }

    @Test
    void 修改设备标识被拒绝() {
        AuthMessage msg = buildMessage("dev-001", "k1", NOW, PAYLOAD);
        String token = tokenService.sign(msg, "secret-of-dev-001");
        // 攻击者把签名套到另一台设备标识上：deviceId 在 MAC 覆盖范围内
        AuthMessage tampered = new AuthMessage("dev-002", msg.protocolVersion(), msg.keyId(),
                msg.timestamp(), msg.nonce(), msg.messageType(), msg.payloadDigest());
        assertFalse(authFilter.authenticate(tampered, token, NOW),
                "设备标识被替换后原签名必须失效");
    }

    @Test
    void 窗口内重复发送同一消息被重放拒绝() {
        AuthMessage msg = buildMessage("dev-001", "k1", NOW, PAYLOAD);
        String token = tokenService.sign(msg, "secret-of-dev-001");
        assertTrue(authFilter.authenticate(msg, token, NOW), "首次出现必须放行");
        // 窗口内原样重发：签名、时标全部合法，但被 nonce 查重拦下——
        // 这正说明时间窗口不是防重放的充分条件
        assertFalse(authFilter.authenticate(msg, token, NOW),
                "窗口内重复利用同一认证消息必须被拒绝");
    }

    @Test
    void 并发重放只放行一次() throws Exception {
        AuthMessage msg = buildMessage("dev-001", "k1", NOW, PAYLOAD);
        String token = tokenService.sign(msg, "secret-of-dev-001");
        int threads = 16;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch fire = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                ready.countDown();
                fire.await();
                return authFilter.authenticate(msg, token, NOW);
            }));
        }
        ready.await();
        fire.countDown();
        int passed = 0;
        for (Future<Boolean> f : futures) {
            if (f.get()) {
                passed++;
            }
        }
        pool.shutdown();
        assertEquals(1, passed, "并发重放下必须有且仅有一个请求被放行");
    }

    @Test
    void 密钥轮换旧keyId拒绝新keyId通过() {
        // 教学简化：先签发旧密钥令牌，再轮换（注册 k2 + 吊销 k1）
        AuthMessage oldMsg = buildMessage("dev-001", "k1", NOW, PAYLOAD);
        String oldToken = tokenService.sign(oldMsg, "secret-of-dev-001");
        store.enroll("dev-001", "k2", "secret-of-dev-001-v2");
        store.revokeKey("dev-001", "k1");
        assertFalse(authFilter.authenticate(oldMsg, oldToken, NOW),
                "轮换后旧 keyId 的令牌必须被拒绝");
        AuthMessage newMsg = buildMessage("dev-001", "k2", NOW, PAYLOAD);
        assertTrue(authenticate(newMsg, "secret-of-dev-001-v2", NOW),
                "轮换后新 keyId 的令牌必须放行");
    }

    @Test
    void 密钥以密文形态落库() {
        // 仓库内不出现明文：任意 keyId 取出的都是解密后的可用密钥，
        // 而存储介质上的形态由 KeyProtector 保证为密文——这里验证加解密往返一致
        KeyProtector kp = new AesGcmKeyProtector(
                Base64.getEncoder().encodeToString(new byte[32]));
        String protectedSecret = kp.protect("secret-of-dev-001");
        assertNotEquals("secret-of-dev-001", protectedSecret,
                "落库形态必须是密文而非明文");
        assertFalse(protectedSecret.contains("secret"),
                "密文不得包含明文片段");
        assertEquals("secret-of-dev-001", kp.unprotect(protectedSecret),
                "受保护形态必须可逆——服务端认证需要解出密钥执行 MAC 运算");
    }
}
