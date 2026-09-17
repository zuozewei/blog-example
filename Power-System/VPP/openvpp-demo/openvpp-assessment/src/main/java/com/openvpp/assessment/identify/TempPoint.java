package com.openvpp.assessment.identify;

/**
 * 温度采样点 —— ETP 参数辨识的输入数据。
 *
 * 教学简化：只承载停机漂移段的核心字段，不携带设备 ID 与质量标志位。
 */
public final class TempPoint {

    private final double elapsedSeconds;
    private final double temp;
    private final double outdoorTemp;

    /**
     * @param elapsedSeconds 停机后经过的秒数
     * @param temp           室内温度（°C）
     * @param outdoorTemp    室外温度（°C）
     */
    public TempPoint(double elapsedSeconds, double temp, double outdoorTemp) {
        this.elapsedSeconds = elapsedSeconds;
        this.temp = temp;
        this.outdoorTemp = outdoorTemp;
    }

    public double elapsedSeconds() { return elapsedSeconds; }
    public double temp() { return temp; }
    public double outdoorTemp() { return outdoorTemp; }
}
