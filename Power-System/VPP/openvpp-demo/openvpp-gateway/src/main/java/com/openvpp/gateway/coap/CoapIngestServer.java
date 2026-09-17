package com.openvpp.gateway.coap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openvpp.gateway.model.DeviceMessage;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.elements.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

/**
 * CoAP 接入服务端 —— 适配低功耗/受限网络设备（NB-IoT 等 UDP 场景）。
 * 上报方式：POST coap://host:port/telemetry?d={deviceId}，载荷为 JSON。
 * CoAP 与 MQTT 复用同一个 DeviceMessage 模型，业务侧无感知。
 */
public class CoapIngestServer extends CoapServer {

    private static final Logger log = LoggerFactory.getLogger(CoapIngestServer.class);

    private final Consumer<DeviceMessage> consumer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CoapIngestServer(int port, Consumer<DeviceMessage> consumer) {
        // Cf 3.x 必须显式注册配置模块，否则报 IllegalState：Configuration contains no values
        super(Configuration.createStandardWithoutFile(), port);
        this.consumer = consumer;
        add(new TelemetryResource());
    }

    private class TelemetryResource extends CoapResource {

        TelemetryResource() {
            super("telemetry");
        }

        @Override
        public void handlePOST(CoapExchange exchange) {
            try {
                // 设备 ID 经查询参数传递：?d=dev-001
                List<String> queries = exchange.getRequestOptions().getUriQuery();
                String deviceId = queries.stream()
                        .filter(q -> q.startsWith("d="))
                        .map(q -> q.substring(2))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("missing device id (?d=)"));

                String body = new String(exchange.getRequestPayload(), StandardCharsets.UTF_8);
                DeviceMessage dm = objectMapper.readValue(body, DeviceMessage.class);
                dm.setProtocol("COAP");
                dm.setDeviceId(deviceId);
                if (dm.getType() == null) {
                    dm.setType(DeviceMessage.Type.TELEMETRY);
                }
                if (dm.getSeq() == null) {
                    dm.setSeq(dm.getTs());
                }
                consumer.accept(dm);

                exchange.respond(CoAP.ResponseCode.CHANGED);
            } catch (Exception e) {
                log.error("CoAP ingest failed: {}", e.getMessage());
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST);
            }
        }
    }
}
