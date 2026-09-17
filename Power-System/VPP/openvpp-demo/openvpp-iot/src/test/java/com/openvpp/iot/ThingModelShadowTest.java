package com.openvpp.iot;

import com.openvpp.iot.interpret.MessageInterpreter;
import com.openvpp.iot.shadow.DeviceShadow;
import com.openvpp.iot.shadow.DeviceShadowRepository;
import com.openvpp.iot.thingmodel.ThingModel;
import com.openvpp.iot.thingmodel.ThingModelRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 物模型与设备影子全链路单测：
 * 模型加载 → 报文解释（三道校验）→ 影子合并（版本校核）→ 期望差异计算。
 */
class ThingModelShadowTest {

    private ThingModelRegistry registry;
    private DeviceShadowRepository shadowRepository;
    private MessageInterpreter interpreter;

    @BeforeEach
    void setUp() {
        registry = ThingModelRegistry.loadBuiltin();
        shadowRepository = new DeviceShadowRepository();
        interpreter = new MessageInterpreter(registry, shadowRepository);
    }

    @Test
    void 物模型加载与属性查找() {
        ThingModel model = registry.require("storage-pcs-v1");
        assertEquals(4, model.getProperties().size());

        ThingModel.Property soc = model.findProperty("soc").orElseThrow();
        assertEquals("%", soc.getUnit());
        assertTrue(soc.inRange(62));
        assertFalse(soc.inRange(120));

        assertFalse(model.findProperty("not-exist").isPresent());
    }

    @Test
    void 报文解释三道校验() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("soc", 62.0);            // 正常
        payload.put("power", 1200.5);        // 正常
        payload.put("unknownPoint", 1);      // 模型外点值：丢弃
        payload.put("temp", 99.0);           // 越界：告警但入库

        Map<String, Object> accepted =
                interpreter.interpretTelemetry("storage-pcs-v1", "dev-001", payload, 100L);

        assertEquals(3, accepted.size());
        assertFalse(accepted.containsKey("unknownPoint"));

        DeviceShadow shadow = shadowRepository.get("dev-001");
        assertEquals(62.0, shadow.getReported().get("soc"));
        assertEquals(100L, shadow.getVersion());
    }

    @Test
    void 影子版本校核旧报文不得覆盖() {
        Map<String, Object> newer = Map.of("soc", 62.0);
        Map<String, Object> stale = Map.of("soc", 10.0);

        interpreter.interpretTelemetry("storage-pcs-v1", "dev-002", newer, 200L);
        interpreter.interpretTelemetry("storage-pcs-v1", "dev-002", stale, 150L);  // 乱序旧报文

        DeviceShadow shadow = shadowRepository.get("dev-002");
        assertEquals(62.0, shadow.getReported().get("soc"), "旧报文覆盖新状态，校核失效");
        assertEquals(200L, shadow.getVersion());
    }

    @Test
    void 期望差异驱动控制闭环() {
        Map<String, Object> reported = Map.of("targetPower", 500.0);
        interpreter.interpretTelemetry("storage-pcs-v1", "dev-003", reported, 1L);

        DeviceShadow shadow = shadowRepository.get("dev-003");
        shadow.mergeDesired(Map.of("targetPower", -800.0));

        Map<String, Object> diff = shadow.pendingDiff();
        assertEquals(-800.0, diff.get("targetPower"), "期望值未生效");

        // 设备执行后回执追平，差异消失 = 控制闭环完成
        interpreter.interpretTelemetry("storage-pcs-v1", "dev-003", Map.of("targetPower", -800.0), 2L);
        assertTrue(shadow.pendingDiff().isEmpty(), "回执追平后差异应清空");
    }
}
