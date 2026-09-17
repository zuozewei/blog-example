package com.openvpp.iot.thingmodel;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 物模型 —— 设备的数字化契约，按"属性/事件/服务"三段式定义。
 * 以 JSON 文档管理（见 resources/thingmodels/*.json），与协议栈解耦：
 * 网关只管搬运字节，物模型告诉平台"power 是什么、单位是什么、范围是多少"。
 */
public class ThingModel {

    private String modelId;
    private String name;
    private List<Property> properties = Collections.emptyList();
    private List<Event> events = Collections.emptyList();
    private List<Service> services = Collections.emptyList();

    /** 属性：可上报可下发的状态点（power/soc/temp） */
    public static class Property {
        private String identifier;
        private String name;
        private String dataType;      // DOUBLE / INT / BOOL / ENUM
        private String unit;
        private Double min;
        private Double max;
        /** 只读（遥测）或可读写（可下发的设定值） */
        private boolean writable;

        public boolean inRange(double value) {
            if (min != null && value < min) {
                return false;
            }
            return max == null || value <= max;
        }

        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public Double getMin() { return min; }
        public void setMin(Double min) { this.min = min; }
        public Double getMax() { return max; }
        public void setMax(Double max) { this.max = max; }
        public boolean isWritable() { return writable; }
        public void setWritable(boolean writable) { this.writable = writable; }
    }

    /** 事件：设备主动上送的瞬时动作（告警/启停/插枪） */
    public static class Event {
        private String identifier;
        private String name;

        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    /** 服务：平台可调用的设备能力（设定温度/调整充电功率/远程启停） */
    public static class Service {
        private String identifier;
        private String name;
        /** 出参属性标识，执行结果回执按此校验 */
        private String outputProperty;

        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getOutputProperty() { return outputProperty; }
        public void setOutputProperty(String outputProperty) { this.outputProperty = outputProperty; }
    }

    public Optional<Property> findProperty(String identifier) {
        return properties.stream().filter(p -> p.getIdentifier().equals(identifier)).findFirst();
    }

    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Property> getProperties() { return properties; }
    public void setProperties(List<Property> properties) { this.properties = properties; }
    public List<Event> getEvents() { return events; }
    public void setEvents(List<Event> events) { this.events = events; }
    public List<Service> getServices() { return services; }
    public void setServices(List<Service> services) { this.services = services; }
}
