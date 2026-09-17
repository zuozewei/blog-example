package com.openvpp.app.config;

import com.openvpp.gateway.coap.CoapIngestServer;
import com.openvpp.gateway.mqtt.MqttIngestService;
import com.openvpp.gateway.model.DeviceMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * 接入网关装配：统一消息消费者 + MQTT/CoAP 双通道。
 * 专栏演示版消费者只打日志；第 8 篇时序链路会把它替换为写入管道。
 */
@Configuration
public class GatewayConfig {

    private static final Logger log = LoggerFactory.getLogger(GatewayConfig.class);

    @Bean
    public Consumer<DeviceMessage> deviceMessageConsumer() {
        return dm -> log.info("[INGRESS] protocol={} device={} type={} seq={} payload={}",
                dm.getProtocol(), dm.getDeviceId(), dm.getType(), dm.getSeq(), dm.getPayload());
    }

    @Bean(destroyMethod = "disconnect")
    public MqttIngestService mqttIngestService(
            @Value("${openvpp.mqtt.broker:tcp://broker-cn.emqx.io:1883}") String broker,
            @Value("${openvpp.mqtt.client-id:openvpp-demo-gw}") String clientId,
            Consumer<DeviceMessage> deviceMessageConsumer) throws Exception {
        MqttIngestService service = new MqttIngestService(broker, clientId, deviceMessageConsumer);
        service.connect();
        return service;
    }

    @Bean(destroyMethod = "stop")
    public CoapIngestServer coapIngestServer(
            @Value("${openvpp.coap.port:5683}") int port,
            Consumer<DeviceMessage> deviceMessageConsumer) {
        CoapIngestServer server = new CoapIngestServer(port, deviceMessageConsumer);
        server.start();
        return server;
    }
}
