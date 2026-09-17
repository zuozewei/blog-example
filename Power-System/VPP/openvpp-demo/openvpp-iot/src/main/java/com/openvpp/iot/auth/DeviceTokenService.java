package com.openvpp.iot.auth;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * 设备令牌服务 —— 一机一密认证的签发与校验，五层语义的承载者。
 *
 * 五层各管一件事，每层职责单一、互不替代：
 *
 *  ① 设备身份认证 —— 证明"发送方是谁"。共享密钥 + HMAC-SHA256：
 *     只有持有该设备密钥的一方能算出这个 MAC。密钥永不上链。
 *     不证明：报文内容是什么（那是第 ② 层的事）。
 *  ② 消息完整性校验 —— 证明"关键字段与正文未被篡改"。MAC 覆盖
 *     AuthMessage 全部七字段（含正文摘要），改任何一处摘要即失配。
 *     注意：MAC 是认证码不是加密，不提供机密性（RFC 2104）。
 *  ③ 防重放 —— 证明"这条认证消息未被重复利用"。nonce + NonceRegistry
 *     已用登记，时间窗口（默认 5 分钟）只负责时效限制，窗口内逐条查重。
 *  ④ 业务幂等 —— 合法重传不导致设备重复动作。属业务层职责（指令 ID 幂等），
 *     与第 ③ 层区别：防重放拦的是"同一条认证消息被原样再发一次"，
 *     业务幂等兜的是"不同 nonce 的合法新消息触发同一业务动作"（断网重传
 *     重新签名后 nonce 必然不同，第 ③ 层放行，由业务层按指令 ID 去重）。
 *  ⑤ 传输保护 —— TLS/加密通道，提供机密性。本篇代码不含此层（网关部署职责），
 *     但必须在链路图上占住位置：MAC 不加密，没有 TLS 报文仍可被嗅探。
 *
 * 令牌模型（与主流 IoT 平台一机一密同构）：
 *   token = HMAC-SHA256(deviceSecret, canonicalForm(authMessage))
 *   canonicalForm = deviceId|protocolVersion|keyId|timestamp|nonce|messageType|payloadDigest
 */
public class DeviceTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final DeviceCredentialStore store;
    private final NonceRegistry nonceRegistry;
    private final long windowMs;

    public DeviceTokenService(DeviceCredentialStore store, NonceRegistry nonceRegistry, long windowMs) {
        this.store = store;
        this.nonceRegistry = nonceRegistry;
        this.windowMs = windowMs;
    }

    /** 设备侧：生成一次性 nonce（真实设备由固件生成，可用随机数或单调计数器） */
    public String newNonce() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return UUID.nameUUIDFromBytes(bytes).toString();
    }

    /**
     * 设备侧：对认证消息计算令牌（演示平台与设备共用此实现，生产中设备为固件实现）。
     * 密钥明文由调用方（设备侧）自持，平台侧永远接触不到。
     */
    public String sign(AuthMessage message, String deviceSecretPlaintext) {
        return HmacSupport.hmacHex(deviceSecretPlaintext, message.canonicalForm());
    }

    /**
     * 平台侧：校验令牌。四道关，按代价从低到高短路求值：
     *   关 1 设备已注册且 keyId 有效（已吊销/未注册在此挡下，连 HMAC 都不用算）；
     *   关 2 协议版本一致（防止降级）；
     *   关 3 时标在窗口内（只做时效限制，不是防重放的充分条件）；
     *   关 4 签名一致（常量时间比对防时序攻击）且 nonce 首次出现（窗口内防重放）。
     * 通过即把 nonce 登记为已用——后续窗口内的同 nonce 重放一律拒绝。
     */
    public boolean verify(AuthMessage message, String token, long nowMs) {
        // 关 2 + 关 3：纯计算，最先做
        if (!AuthMessage.PROTOCOL_VERSION.equals(message.protocolVersion())) {
            return false;
        }
        if (Math.abs(nowMs - message.timestamp()) > windowMs) {
            return false;
        }
        // 关 1：取密钥（此处才解密密钥，未注册设备不触发解密运算）
        return store.secretOf(message.deviceId(), message.keyId())
                // 关 4：MAC 比对 + nonce 原子登记
                // nonce 有效期以消息时间戳为基准：覆盖该消息最晚可接受时刻
                //（设备时钟允许超前至 now + windowMs），过期后原消息重放仍被拒
                .map(secret -> HmacSupport.constantTimeEquals(sign(message, secret), token)
                        && nonceRegistry.registerOnce(message.deviceId(), message.nonce(),
                                message.timestamp(), nowMs))
                .orElse(false);
    }
}
