package com.openvpp.iot.interpret;

import com.openvpp.iot.shadow.DeviceShadow;
import com.openvpp.iot.shadow.DeviceShadowRepository;
import com.openvpp.iot.thingmodel.ThingModel;
import com.openvpp.iot.thingmodel.ThingModelRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 报文解释器 —— 把网关送来的"无名点值"翻译成物模型语义。
 * 这是物联平台的灵魂工序：没有它，power=1200.5 只是个数字；
 * 有了它，平台知道这是"储能 PCS 的当前功率，单位 kW，合法范围 ±2000"。
 * 翻译三道关：模型注册校验 → 属性存在校验 → 量纲范围校验。
 */
public class MessageInterpreter {

    private static final Logger log = LoggerFactory.getLogger(MessageInterpreter.class);

    private final ThingModelRegistry registry;
    private final DeviceShadowRepository shadowRepository;

    public MessageInterpreter(ThingModelRegistry registry, DeviceShadowRepository shadowRepository) {
        this.registry = registry;
        this.shadowRepository = shadowRepository;
    }

    /**
     * 解释遥测报文并写入影子。
     *
     * @param modelId  设备绑定的物模型（设备档案侧维护，此处演示直传）
     * @param deviceId 设备 ID
     * @param payload  原始点值
     * @param seq      报文序列号（影子版本校核用）
     * @return 通过校验、实际入影子的属性子集
     */
    public Map<String, Object> interpretTelemetry(String modelId, String deviceId,
                                                  Map<String, Object> payload, long seq) {
        ThingModel model = registry.require(modelId);
        Map<String, Object> accepted = new HashMap<>();

        payload.forEach((identifier, rawValue) -> model.findProperty(identifier).ifPresentOrElse(property -> {
            if (rawValue instanceof Number && !property.inRange(((Number) rawValue).doubleValue())) {
                // 越界点值：可信但需告警，仍然入库（运维决策，非数据错误）
                log.warn("点值越界: device={} property={} value={} range=[{},{}]",
                        deviceId, identifier, rawValue, property.getMin(), property.getMax());
            }
            accepted.put(identifier, rawValue);
        }, () -> log.warn("模型外点值被丢弃: device={} model={} 未知属性={}", deviceId, modelId, identifier)));

        DeviceShadow shadow = shadowRepository.getOrCreate(deviceId, modelId);
        shadow.mergeReported(accepted, seq);
        return accepted;
    }
}
