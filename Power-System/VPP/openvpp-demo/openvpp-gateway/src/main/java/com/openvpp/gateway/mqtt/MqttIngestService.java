package com.openvpp.gateway.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openvpp.gateway.model.DeviceMessage;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * MQTT 接入服务 —— 设备遥测上行主通道。
 * 专栏演示版：订阅统一上行 topic，解析为 DeviceMessage 后交给上层消费。
 * 生产级还需补：共享订阅负载均衡、离线消息 QoS 策略、断连退避重连参数化。
 */
public class MqttIngestService {

    private static final Logger log = LoggerFactory.getLogger(MqttIngestService.class);

    /** 统一上行 topic 规范：openvpp/{deviceId}/telemetry|event|ack */
    public static final String TOPIC_PATTERN = "openvpp/+/+";

    private final MqttClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MqttIngestService(String brokerUrl, String clientId,
                             Consumer<DeviceMessage> consumer) throws Exception {
        this.client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());

        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                // 断连自动重连成功后必须重新订阅，否则消息静默丢失
                subscribeQuietly();
            }

            @Override
            public void connectionLost(Throwable cause) {
                log.warn("MQTT connection lost: {}", cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                try {
                    String body = new String(message.getPayload(), StandardCharsets.UTF_8);
                    DeviceMessage dm = parse(topic, body);
                    consumer.accept(dm);
                } catch (Exception e) {
                    // 单条报文解析失败不能打断整个消费循环
                    log.error("Failed to handle message from {}: {}", topic, e.getMessage());
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // 演示版不使用下行发布确认
            }
        });
    }

    public void connect() throws Exception {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(30);
        client.connect(options);
        subscribeQuietly();
    }

    private void subscribeQuietly() {
        try {
            client.subscribe(TOPIC_PATTERN, 1);
            log.info("MQTT subscribed: {}", TOPIC_PATTERN);
        } catch (Exception e) {
            log.error("MQTT subscribe failed: {}", e.getMessage());
        }
    }

    /**
     * topic 三段式解析 + JSON 载荷反序列化。
     * 序列号 seq 缺失时以时间戳兜底，保证校核字段非空。
     */
    DeviceMessage parse(String topic, String body) throws Exception {
        String[] segments = topic.split("/");
        if (segments.length != 3) {
            throw new IllegalArgumentException("Illegal topic: " + topic);
        }
        DeviceMessage dm = objectMapper.readValue(body, DeviceMessage.class);
        dm.setProtocol("MQTT");
        dm.setDeviceId(segments[1]);
        dm.setType(parseType(segments[2]));
        if (dm.getSeq() == null) {
            dm.setSeq(dm.getTs());
        }
        return dm;
    }

    private DeviceMessage.Type parseType(String segment) {
        switch (segment) {
            case "telemetry":
                return DeviceMessage.Type.TELEMETRY;
            case "event":
                return DeviceMessage.Type.EVENT;
            case "ack":
                return DeviceMessage.Type.CONTROL_ACK;
            default:
                throw new IllegalArgumentException("Unknown topic type: " + segment);
        }
    }

    public void disconnect() throws Exception {
        if (client.isConnected()) {
            client.disconnect();
        }
        client.close();
    }
}
