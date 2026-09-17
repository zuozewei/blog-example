package com.openvpp.iot.auth;

import java.util.List;

/**
 * 设备凭据记录 —— 一台设备的密钥在仓库中的完整形态。
 * 支持多版本并存（密钥轮换：新 keyId 启用与旧 keyId 吊销之间允许重叠灰度期），
 * 吊销的记录保留在仓库内只用于审计追溯，运行期不再参与认证。
 * 不可变对象：吊销通过生成副本实现，避免并发读到半更新状态。
 */
public final class CredentialRecord {

    /** 设备唯一标识 */
    private final String deviceId;

    /**
     * 密钥标识（轮换后递增，如 "k1"/"k2"；令牌中携带，
     * 平台按 deviceId + keyId 定位密钥，避免轮换期密文混淆）
     */
    private final String keyId;

    /**
     * 受保护的密钥形态（密文，经 KeyProtector 加密后落库；
     * 注意：不是哈希——服务端认证时必须能解出明文执行 MAC 运算）
     */
    private final String protectedSecret;

    /** 是否已吊销（吊销即时生效，设备丢失/解约的第一动作） */
    private final boolean revoked;

    public CredentialRecord(String deviceId, String keyId, String protectedSecret, boolean revoked) {
        this.deviceId = deviceId;
        this.keyId = keyId;
        this.protectedSecret = protectedSecret;
        this.revoked = revoked;
    }

    public String deviceId() {
        return deviceId;
    }

    public String keyId() {
        return keyId;
    }

    public String protectedSecret() {
        return protectedSecret;
    }

    public boolean revoked() {
        return revoked;
    }

    /** 返回吊销态副本（密钥轮换/吊销均通过替换记录实现，保证不可变语义） */
    public CredentialRecord revoke() {
        return new CredentialRecord(deviceId, keyId, protectedSecret, true);
    }

    /** 设备当前有效记录：未吊销记录中按注册顺序取最新一条 */
    public static CredentialRecord currentOf(List<CredentialRecord> records) {
        CredentialRecord current = null;
        for (CredentialRecord r : records) {
            if (!r.revoked()) {
                current = r;
            }
        }
        return current;
    }
}
