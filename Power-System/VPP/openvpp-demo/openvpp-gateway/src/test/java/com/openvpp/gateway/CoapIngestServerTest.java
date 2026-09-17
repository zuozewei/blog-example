package com.openvpp.gateway;

import com.openvpp.gateway.coap.CoapIngestServer;
import com.openvpp.gateway.model.DeviceMessage;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * CoAP 接入回环实测：本机 127.0.0.1 起服务端并 POST 一条遥测，
 * 断言服务端正确解析为统一报文并应答 CHANGED。
 */
class CoapIngestServerTest {

    @Test
    void telemetryLoopback() throws Exception {
        BlockingQueue<DeviceMessage> inbox = new LinkedBlockingQueue<>();
        CoapIngestServer server = new CoapIngestServer(0, inbox::offer);  // 端口 0 = 随机空闲端口
        server.start();
        int port = server.getEndpoints().get(0).getAddress().getPort();

        String body = "{\"ts\":1758000000000,\"payload\":{\"power\":86.4}}";
        CoapClient client = new CoapClient("coap://127.0.0.1:" + port + "/telemetry?d=dev-coap-01");
        CoapResponse response = client.post(body.getBytes(StandardCharsets.UTF_8),
                MediaTypeRegistry.APPLICATION_JSON);

        assertNotNull(response, "CoAP 服务端无应答");
        assertEquals(CoAP.ResponseCode.CHANGED, response.getCode());

        DeviceMessage dm = inbox.poll(5, TimeUnit.SECONDS);
        assertNotNull(dm, "5s 内未收到 CoAP 回环消息");
        assertEquals("COAP", dm.getProtocol());
        assertEquals("dev-coap-01", dm.getDeviceId());
        assertEquals(DeviceMessage.Type.TELEMETRY, dm.getType());
        assertEquals(86.4, ((Number) dm.getPayload().get("power")).doubleValue());

        server.stop();
        server.destroy();
    }
}
