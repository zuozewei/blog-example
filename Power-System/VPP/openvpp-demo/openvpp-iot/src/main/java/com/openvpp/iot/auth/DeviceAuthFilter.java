package com.openvpp.iot.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 设备认证过滤器 —— 接入层的强制闸口。
 * 任何上行报文先过认证，未注册设备、非法令牌、重放报文在此拦截，
 * 非法流量不得进入物模型解释与影子写入（44260 数据校核的第一道门）。
 *
 * 与真实接入链路的衔接现状（如实标注）：
 * 本过滤器位于 openvpp-iot 模块，而 MQTT/CoAP 协议适配在 openvpp-gateway
 * （MqttIngestService / CoapIngestServer）。网关解析出 DeviceMessage 后，
 * 生产形态应由网关消费者先构造 AuthMessage（deviceId 取自 topic、正文摘要
 * 取自原始报文体、keyId/nonce/token 取自报文头字段）再调用本过滤器，
 * 认证通过才放行到解释层。当前演示工程中网关尚未接线调用本过滤器——
 * 专栏在"生产扩展条件"一节如实说明这一差距，生产接入必须完成此接线并
 * 以端到端用例验证（篡改正文/重放报文在网关入口即被拒）。
 */
public class DeviceAuthFilter {

    private static final Logger log = LoggerFactory.getLogger(DeviceAuthFilter.class);

    private final DeviceTokenService tokenService;

    public DeviceAuthFilter(DeviceTokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * 认证入口。通过返回 true，拦截返回 false 并记审计日志。
     * 审计字段不含令牌与密钥，防日志泄露。
     */
    public boolean authenticate(AuthMessage message, String token, long nowMs) {
        boolean pass = tokenService.verify(message, token, nowMs);
        if (!pass) {
            log.warn("[AUTH-REJECT] device={} keyId={} nonce={} ts={} now={}",
                    message.deviceId(), message.keyId(), message.nonce(),
                    message.timestamp(), nowMs);
        }
        return pass;
    }
}
