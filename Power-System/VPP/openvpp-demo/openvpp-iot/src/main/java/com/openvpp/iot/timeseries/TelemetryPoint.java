package com.openvpp.iot.timeseries;

/**
 * 遥测点 —— 时序存储的最小写入单元。
 * 经过物模型解释后的数值型属性才会进入时序链路：
 * 非数值点（布尔/枚举/文本）走事件通道，不占时序库。
 */
public class TelemetryPoint {

    private final String deviceId;
    private final String property;
    private final long tsMs;
    private final double value;

    public TelemetryPoint(String deviceId, String property, long tsMs, double value) {
        this.deviceId = deviceId;
        this.property = property;
        this.tsMs = tsMs;
        this.value = value;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getProperty() {
        return property;
    }

    public long getTsMs() {
        return tsMs;
    }

    public double getValue() {
        return value;
    }
}
