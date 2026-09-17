package com.openvpp.app.config;

import com.openvpp.gateway.coap.CoapIngestServer;
import com.openvpp.gateway.mqtt.MqttIngestService;
import com.openvpp.gateway.model.DeviceMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * 接入网关装配：统一消息消费者 + MQTT/CoAP 双通道。
 *
 * 接入模式由 openvpp.gateway.mode 控制：
 *   local（默认）：本地模拟模式，不创建任何协议接入 Bean，应用零外部依赖即可启动，
 *                 断网环境下正常运行；贯穿案例的遥测/计量由编排层内置模拟源驱动。
 *   remote       ：真实协议接入，须显式配置地址（MQTT 须 openvpp.mqtt.broker，
 *                 CoAP 端口 openvpp.coap.port 默认 5683），否则启动即报错。
 */
@Configuration
public class GatewayConfig {

    private static final Logger log = LoggerFactory.getLogger(GatewayConfig.class);

    /**
     * 演示消费者仅打印日志、不驱动业务链路，标识 [INGRESS-MOCK] 以示模拟接入，
     * 避免被误读为真实遥测；贯穿案例数据由编排层内置模拟源构造。
     * 第 8 篇时序链路会把它替换为写入管道。
     */
    @Bean
    public Consumer<DeviceMessage> deviceMessageConsumer(
            @Value("${openvpp.gateway.mode:local}") String mode) {
        if ("remote".equals(mode)) {
            log.info("网关远程接入模式：MQTT/CoAP 协议通道已启用");
        } else {
            log.info("网关本地模拟模式（openvpp.gateway.mode=local）：未启用真实协议接入，"
                    + "遥测/计量由内置模拟源驱动");
        }
        return dm -> log.info("[INGRESS-MOCK] protocol={} device={} type={} seq={} payload={}",
                dm.getProtocol(), dm.getDeviceId(), dm.getType(), dm.getSeq(), dm.getPayload());
    }

    /** 仅 remote 模式创建并连接；broker 必须显式配置，杜绝默认连公网服务 */
    @Bean(destroyMethod = "disconnect")
    @ConditionalOnProperty(prefix = "openvpp.gateway", name = "mode", havingValue = "remote")
    public MqttIngestService mqttIngestService(
            @Value("${openvpp.mqtt.broker:}") String broker,
            @Value("${openvpp.mqtt.client-id:openvpp-demo-gw}") String clientId,
            Consumer<DeviceMessage> deviceMessageConsumer) throws Exception {
        if (broker == null || broker.isBlank()) {
            throw new IllegalStateException(
                    "openvpp.gateway.mode=remote 时必须显式配置 openvpp.mqtt.broker");
        }
        MqttIngestService service = new MqttIngestService(broker, clientId, deviceMessageConsumer);
        service.connect();
        return service;
    }

    /** 仅 remote 模式启动 CoAP 服务端（本地模拟模式不占用 UDP 端口） */
    @Bean(destroyMethod = "stop")
    @ConditionalOnProperty(prefix = "openvpp.gateway", name = "mode", havingValue = "remote")
    public CoapIngestServer coapIngestServer(
            @Value("${openvpp.coap.port:5683}") int port,
            Consumer<DeviceMessage> deviceMessageConsumer) {
        CoapIngestServer server = new CoapIngestServer(port, deviceMessageConsumer);
        server.start();
        return server;
    }
}
