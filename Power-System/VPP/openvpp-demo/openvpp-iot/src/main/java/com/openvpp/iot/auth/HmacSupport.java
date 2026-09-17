package com.openvpp.iot.auth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * HMAC/SHA-256 底层运算与常量时间比对 —— 令牌服务内部复用的算法底座。
 *
 * 概念边界（本篇必须讲清的一点）：HMAC 是**消息认证码（MAC）**，
 * 提供的是"持有密钥者生成 + 未被篡改"的证明（RFC 2104），
 * 它**不是加密**——不提供机密性，任何人都能读到报文原文。
 * 机密性是传输层 TLS 的职责（第 ⑤ 层），两者不可混为一谈。
 */
final class HmacSupport {

    private static final String HMAC_ALGO = "HmacSHA256";
    private static final String SHA_ALGO = "SHA-256";

    private HmacSupport() {
    }

    static String hmacHex(String secret, String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            return toHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC 计算失败", e);
        }
    }

    static String sha256Hex(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance(SHA_ALGO);
            return toHex(md.digest(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 计算失败", e);
        }
    }

    /**
     * 常量时间比对：不用 String.equals——后者逐字符短路返回，
     * 响应时间差会泄漏"前缀匹配了多少位"，给时序攻击留门。
     */
    static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}
