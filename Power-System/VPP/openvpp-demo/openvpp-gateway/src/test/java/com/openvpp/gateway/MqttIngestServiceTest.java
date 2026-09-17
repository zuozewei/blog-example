package com.openvpp.gateway;

import com.openvpp.gateway.model.DeviceMessage;
import com.openvpp.gateway.mqtt.MqttIngestService;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * MQTT 接入回环实测：经公共测试 broker（broker-cn.emqx.io）发布一条遥测，
 * 断言 MqttIngestService 能正确解析为统一报文。
 * 对应专栏第 02 篇的方法论：标准条款（44260 数据校核）对应可执行测试。
 */
class MqttIngestServiceTest {

    private static final String BROKER = "tcp://broker-cn.emqx.io:1883";

    @Test
    void telemetryLoopback() throws Exception {
        BlockingQueue<DeviceMessage> inbox = new LinkedBlockingQueue<>();
        MqttIngestService service = new MqttIngestService(BROKER, "openvpp-demo-test-sub", inbox::offer);
        service.connect();

        String body = "{\"ts\":1758000000000,\"seq\":42,\"payload\":{\"power\":1200.5,\"soc\":0.62}}";
        MqttClient publisher = new MqttClient(BROKER, "openvpp-demo-test-pub", new MemoryPersistence());
        publisher.connect();
        publisher.publish("openvpp/dev-001/telemetry",
                new MqttMessage(body.getBytes(StandardCharsets.UTF_8)));
        publisher.disconnect();
        publisher.close();

        DeviceMessage dm = inbox.poll(20, TimeUnit.SECONDS);
        assertNotNull(dm, "20s 内未收到 MQTT 回环消息");
        assertEquals("MQTT", dm.getProtocol());
        assertEquals("dev-001", dm.getDeviceId());
        assertEquals(DeviceMessage.Type.TELEMETRY, dm.getType());
        assertEquals(42L, dm.getSeq());
        assertEquals(1200.5, ((Number) dm.getPayload().get("power")).doubleValue());

        service.disconnect();
    }
}
