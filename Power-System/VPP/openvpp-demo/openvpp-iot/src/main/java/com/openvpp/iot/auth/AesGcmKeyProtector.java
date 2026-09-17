package com.openvpp.iot.auth;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM 教学版密钥保护器（教学简化，非生产方案）。
 *
 * 主密钥来源：环境变量 OPENVPP_MASTER_KEY，Base64 编码的 32 字节。
 * 生成示例：openssl rand -base64 32
 *
 * 输出格式：Base64(iv ‖ ciphertext ‖ GCM tag)。
 * 选 GCM 而非 CBC：认证加密（AEAD），密文被篡改时解密直接抛异常，
 * 顺带为落库密文提供完整性校验，不需要再叠加一层 MAC。
 *
 * 与生产 KMS/信封加密的差距（如实标注）：
 *  1. 主密钥以环境变量形式驻留应用进程，无 HSM/云 KMS 托管；
 *  2. 无主密钥轮换与多版本并存机制；
 *  3. 无解密调用审计与访问控制（生产应审计"谁在何时解了哪台设备的密钥"）。
 */
public class AesGcmKeyProtector implements KeyProtector {

    public static final String MASTER_KEY_ENV = "OPENVPP_MASTER_KEY";

    private static final String CIPHER_ALGO = "AES/GCM/NoPadding";
    private static final int IV_LEN = 12;          // GCM 推荐 96 位 IV
    private static final int TAG_BITS = 128;

    private final SecretKeySpec masterKey;
    private final SecureRandom random = new SecureRandom();

    public AesGcmKeyProtector(String base64MasterKey) {
        if (base64MasterKey == null || base64MasterKey.isBlank()) {
            throw new IllegalStateException(
                    "缺少主密钥环境变量 " + MASTER_KEY_ENV + "（Base64 编码 32 字节）");
        }
        byte[] key = Base64.getDecoder().decode(base64MasterKey);
        if (key.length != 32) {
            throw new IllegalStateException("主密钥必须为 32 字节（AES-256），实际 " + key.length + " 字节");
        }
        this.masterKey = new SecretKeySpec(key, "AES");
    }

    /** 从环境变量读取主密钥的默认构造 */
    public static AesGcmKeyProtector fromEnv() {
        return new AesGcmKeyProtector(System.getenv(MASTER_KEY_ENV));
    }

    @Override
    public String protect(String plaintextSecret) {
        try {
            byte[] iv = new byte[IV_LEN];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, masterKey, new GCMParameterSpec(TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintextSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] out = new byte[IV_LEN + ciphertext.length];
            System.arraycopy(iv, 0, out, 0, IV_LEN);
            System.arraycopy(ciphertext, 0, out, IV_LEN, ciphertext.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("密钥加密失败", e);
        }
    }

    @Override
    public String unprotect(String protectedSecret) {
        try {
            byte[] in = Base64.getDecoder().decode(protectedSecret);
            byte[] iv = new byte[IV_LEN];
            System.arraycopy(in, 0, iv, 0, IV_LEN);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
            cipher.init(Cipher.DECRYPT_MODE, masterKey, new GCMParameterSpec(TAG_BITS, iv));
            byte[] plain = cipher.doFinal(in, IV_LEN, in.length - IV_LEN);
            return new String(plain, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            // GCM tag 校验失败（篡改/主密钥不符）与格式损坏统一在此暴露
            throw new IllegalStateException("密钥解密失败：密文损坏或主密钥不匹配", e);
        }
    }
}
