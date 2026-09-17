package com.openvpp.iot.thingmodel;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 物模型注册表 —— 启动时从 classpath:thingmodels/ 加载全部模型文档，
 * 运行期只读。模型变更走"发版+评审"，不提供运行时改模型的后门：
 * 物模型是平台与设备双方的契约，单方面热改就是生产事故。
 */
public class ThingModelRegistry {

    private static final String[] BUILTIN_MODELS = {
            "storage-pcs-v1"
    };

    private final Map<String, ThingModel> models = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static ThingModelRegistry loadBuiltin() {
        ThingModelRegistry registry = new ThingModelRegistry();
        for (String name : BUILTIN_MODELS) {
            String path = "/thingmodels/" + name + ".json";
            try (InputStream in = ThingModelRegistry.class.getResourceAsStream(path)) {
                if (in == null) {
                    throw new IllegalStateException("物模型文档缺失: " + path);
                }
                ThingModel model = registry.objectMapper.readValue(in, ThingModel.class);
                registry.models.put(model.getModelId(), model);
            } catch (Exception e) {
                throw new IllegalStateException("物模型加载失败: " + path, e);
            }
        }
        return registry;
    }

    public Optional<ThingModel> find(String modelId) {
        return Optional.ofNullable(models.get(modelId));
    }

    public ThingModel require(String modelId) {
        return find(modelId).orElseThrow(() ->
                new IllegalArgumentException("未注册的物模型: " + modelId));
    }
}
