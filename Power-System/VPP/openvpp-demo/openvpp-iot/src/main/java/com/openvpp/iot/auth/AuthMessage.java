package com.openvpp.iot.auth;

/**
 * 设备认证消息 —— 待认证上行的完整输入。
 *
 * 为什么引入这个对象：原实现 MAC 只覆盖 "deviceId|timestamp"，
 * 正文、消息类型等关键字段不在签名范围内，攻击者保留签名、篡改正文即可绕过
 * 完整性校验。认证消息把 MAC 覆盖的字段全部收进来，签名先对这些字段求
 * 规范化串再计算，杜绝"签名合法但正文被换"的空档。
 *
 * MAC 覆盖字段清单（顺序固定，设备侧与平台侧必须一致）：
 *   deviceId | protocolVersion | keyId | timestamp | nonce | messageType | payloadDigest
 * 其中 payloadDigest = SHA-256(正文原文) 的十六进制串——正文不直接进 MAC
 * 输入而是先摘要，是为了让 MAC 输入长度恒定、正文编码差异（空白/换行）可单独治理。
 *
 * 不可变对象，构造即完整。
 */
public final class AuthMessage {

    /** 当前协议版本，版本不一致直接拒绝（防止降级到旧版弱校验逻辑） */
    public static final String PROTOCOL_VERSION = "v1";

    private final String deviceId;
    private final String protocolVersion;
    private final String keyId;
    private final long timestamp;

    /**
     * 一次性随机数：防重放的唯一性凭证（第 ③ 层）。
     * 设备侧每条消息生成一个，服务端登记已用记录，窗口内重复出现即拒绝。
     */
    private final String nonce;

    /** 消息类型（telemetry/event/ack），纳入 MAC 防止攻击者改类型换语义 */
    private final String messageType;

    /** 正文摘要：SHA-256(正文) 十六进制，正文被篡改则摘要失配 */
    private final String payloadDigest;

    public AuthMessage(String deviceId, String protocolVersion, String keyId, long timestamp,
                       String nonce, String messageType, String payloadDigest) {
        this.deviceId = deviceId;
        this.protocolVersion = protocolVersion;
        this.keyId = keyId;
        this.timestamp = timestamp;
        this.nonce = nonce;
        this.messageType = messageType;
        this.payloadDigest = payloadDigest;
    }

    public String deviceId() {
        return deviceId;
    }

    public String protocolVersion() {
        return protocolVersion;
    }

    public String keyId() {
        return keyId;
    }

    public long timestamp() {
        return timestamp;
    }

    public String nonce() {
        return nonce;
    }

    public String messageType() {
        return messageType;
    }

    public String payloadDigest() {
        return payloadDigest;
    }

    /** MAC 规范化输入：七字段定序拼接，竖线分隔 */
    public String canonicalForm() {
        return String.join("|", deviceId, protocolVersion, keyId,
                String.valueOf(timestamp), nonce, messageType, payloadDigest);
    }

    /** 计算正文摘要的工具方法（设备侧与接入层共用同一口径） */
    public static String digestOf(String payload) {
        return HmacSupport.sha256Hex(payload);
    }
}
