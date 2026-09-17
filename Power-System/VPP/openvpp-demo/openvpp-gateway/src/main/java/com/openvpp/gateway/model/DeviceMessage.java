package com.openvpp.gateway.model;

import java.util.Map;

/**
 * 统一设备报文 —— 协议适配层与业务层的边界对象。
 * 无论南向是 MQTT 还是 CoAP，进入业务侧都只有这一种形态：
 * 协议差异被关在网关内，业务模块不感知接入协议。
 */
public class DeviceMessage {

    /** 报文类型：遥测/遥信/控制回执 */
    public enum Type {
        TELEMETRY, EVENT, CONTROL_ACK
    }

    /** 接入协议标识：MQTT / COAP，仅作审计溯源用 */
    private String protocol;

    /** 设备唯一标识（物模型设备 ID） */
    private String deviceId;

    private Type type;

    /** 采集时刻（设备侧时标，毫秒） */
    private Long ts;

    /** 负荷点值：如 power=1200.5 / soc=0.62 / temp=24.5 */
    private Map<String, Object> payload;

    /** 报文完整性序列号 —— 对应 44260 配置要求第 4 条"数据校核" */
    private Long seq;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }
}
