package com.openvpp.iot.auth;

/**
 * 密钥保护器 —— 设备密钥的"加密保存"抽象。
 *
 * 设计背景：共享密钥（HMAC 类）认证中，服务端必须用原始密钥执行同样的 MAC 运算，
 * 因此密钥**不能像用户密码那样落库前做不可逆哈希**——哈希之后服务端自己也算不出 MAC，
 * 认证流程直接断掉。正确模型是"可逆但密钥受控"：
 *
 *  - 生产形态：KMS / 信封加密（envelope encryption）——
 *    每设备数据密钥经 KMS 主密钥加密后落库，运行时经 KMS API 解密使用，
 *    主密钥不出 KMS，落库泄露不等于密钥泄露；
 *  - 本演示实现 {@link AesGcmKeyProtector}：AES-256-GCM，主密钥从环境变量
 *    OPENVPP_MASTER_KEY（Base64 编码 32 字节）读取，每次加密生成随机 IV，
 *    输出 Base64(iv ‖ ciphertext ‖ tag)。属教学简化：主密钥驻留环境变量、
 *    无密钥轮换、无访问审计，仅用于说明"密文落库 + 运行时解密"的模型，
 *    严禁作为生产方案照抄。
 */
public interface KeyProtector {

    /**
     * 加密设备密钥明文，返回可落库的受保护形态（密文）。
     */
    String protect(String plaintextSecret);

    /**
     * 解出设备密钥明文，供 MAC 运算使用。
     * 解密结果只应存在于调用栈内，用完即弃，不得二次落库或记日志。
     *
     * @throws IllegalStateException 密文损坏、被篡改或主密钥不匹配时抛出
     */
    String unprotect(String protectedSecret);
}
