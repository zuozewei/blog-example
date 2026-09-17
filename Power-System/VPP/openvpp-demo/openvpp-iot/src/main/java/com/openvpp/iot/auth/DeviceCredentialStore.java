package com.openvpp.iot.auth;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 设备密钥仓库 —— 一机一密的凭据底座。
 *
 * 存储模型（修正"落库前哈希"的错误表述）：
 * 共享密钥认证要求服务端能执行与设备侧相同的 MAC 运算，密钥必须以
 * **可逆但受保护**的形态保存——本仓库只存 KeyProtector 加密后的密文，
 * 密钥明文不出现在仓库中（也不是不可逆哈希：哈希后服务端自己无法完成认证）。
 * 生产形态为 KMS/信封加密托管的密钥表，见 KeyProtector 接口注释。
 *
 * 密钥轮换模型：一台设备可有多条记录（不同 keyId），enroll 追加新版本；
 * revoke(deviceId) 吊销全部版本（设备退役），revokeKey(deviceId, keyId)
 * 只吊销单个版本（轮换收尾时关闭旧密钥）。
 *
 * 演示版内存存储 + AES-GCM 主密钥加密，教学简化；生产为 DB 密文表 + KMS。
 */
public class DeviceCredentialStore {

    private final Map<String, List<CredentialRecord>> credentials = new ConcurrentHashMap<>();
    private final KeyProtector keyProtector;

    public DeviceCredentialStore(KeyProtector keyProtector) {
        this.keyProtector = keyProtector;
    }

    /**
     * 注册/轮换密钥：明文密钥入参只在调用栈内存在，加密后以密文落库。
     * 同 keyId 重复注册视为换绑（覆盖该版本并恢复为有效态）。
     */
    public void enroll(String deviceId, String keyId, String deviceSecretPlaintext) {
        String protectedSecret = keyProtector.protect(deviceSecretPlaintext);
        List<CredentialRecord> records =
                credentials.computeIfAbsent(deviceId, k -> new CopyOnWriteArrayList<>());
        records.removeIf(r -> r.keyId().equals(keyId));
        records.add(new CredentialRecord(deviceId, keyId, protectedSecret, false));
    }

    /** 设备退役/丢失/解约：吊销全部版本，即时生效 */
    public void revoke(String deviceId) {
        credentials.computeIfPresent(deviceId, (k, records) -> {
            records.replaceAll(CredentialRecord::revoke);
            return records;
        });
    }

    /** 密钥轮换收尾：只吊销旧版本，新版本继续服务 */
    public void revokeKey(String deviceId, String keyId) {
        credentials.computeIfPresent(deviceId, (k, records) -> {
            records.replaceAll(r -> r.keyId().equals(keyId) ? r.revoke() : r);
            return records;
        });
    }

    /**
     * 按令牌携带的 keyId 取密钥明文（解出后仅供本次 MAC 运算，调用方不得留存）。
     * 已吊销、不存在的版本一律返回空。
     */
    public Optional<String> secretOf(String deviceId, String keyId) {
        List<CredentialRecord> records = credentials.get(deviceId);
        if (records == null) {
            return Optional.empty();
        }
        return records.stream()
                .filter(r -> r.keyId().equals(keyId) && !r.revoked())
                .findFirst()
                .map(r -> keyProtector.unprotect(r.protectedSecret()));
    }

    /** 设备当前有效密钥标识（设备侧演示签发令牌时使用；真实设备密钥在固件里） */
    public Optional<String> currentKeyId(String deviceId) {
        List<CredentialRecord> records = credentials.get(deviceId);
        if (records == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(CredentialRecord.currentOf(records)).map(CredentialRecord::keyId);
    }

    public boolean enrolled(String deviceId) {
        return currentKeyId(deviceId).isPresent();
    }
}
