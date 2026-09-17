package com.openvpp.assessment.pv;

import java.time.LocalDateTime;

/**
 * 光伏辐照转换链预测器 —— 第 04 篇物理模型的代码实现。
 *
 * 四层串联，每层对应一个物理环节和一类误差来源：
 * 1. 天文层（地外辐照）：日地关系纯几何计算，无不确定性；
 * 2. 大气层（地面辐照）：云量系数衰减，预测误差的最大来源；
 * 3. 组件层（直流转交流）：倾角余弦 + 温度损耗（正午最强光反而被热损耗吃掉 ~10%）；
 * 4. 逆变器层（并网功率）：效率随负载率变化。
 *
 * 输出 P_50 + P_10/P_90 概率区间，区间宽度由天气分类驱动。
 */
public class PvPowerForecaster {

    /** 太阳常数 W/m² */
    private static final double G_SC = 1367.0;
    /** 标准测试条件辐照 W/m² */
    private static final double G_STC = 1000.0;
    /** 标准测试条件温度 °C */
    private static final double T_STC = 25.0;
    /** 晴天大气透过率：地外辐照经大气衰减到地面的固定折扣（与云量系数分属两层） */
    private static final double CLEAR_SKY_TRANSMITTANCE = 0.68;
    /** 散射+反射分量占直射的近似比例 */
    private static final double DIFFUSE_RATIO = 0.10;

    private final double capacityKwp;
    private final double latitude;
    private final double tiltDeg;
    private final double tempCoeffPct;

    public PvPowerForecaster(double capacityKwp, double latitude, double tiltDeg, double tempCoeffPct) {
        this.capacityKwp = capacityKwp;
        this.latitude = latitude;
        this.tiltDeg = tiltDeg;
        this.tempCoeffPct = tempCoeffPct;
    }

    /**
     * 单时刻出力预测。
     *
     * @param time         预测时刻
     * @param weather      天气分类（云量系数与置信度来源）
     * @param ambientTempC 环境温度 °C
     */
    public ForecastCurve forecast(LocalDateTime time, WeatherClass weather, double ambientTempC) {
        double gEx = extraterrestrialIrradiance(time);
        // 大气层：先过晴天透过率（空气分子/气溶胶固定衰减），再过云量系数（天气变化）
        double gGround = gEx * CLEAR_SKY_TRANSMITTANCE * weather.getCloudFactor();
        double gEff = gGround * Math.cos(Math.toRadians(tiltDeg - optimalTilt()))
                + gGround * DIFFUSE_RATIO;   // 散射+反射分量近似
        double tCell = ambientTempC + (gEff / 800.0) * 20.0;   // NOCT 近似
        double pDc = capacityKwp * (gEff / G_STC) * (1 + tempCoeffPct / 100.0 * (tCell - T_STC));
        double p50 = pDc * inverterEfficiency(pDc);

        // 概率区间：天气越差区间越宽。以置信度反推半宽比例
        double halfWidthRatio = (1 - weather.getConfidence()) * 0.9;
        double p90 = Math.max(0, p50 * (1 - halfWidthRatio));
        double p10 = p50 * (1 + halfWidthRatio * 0.5);

        return new ForecastCurve(time, p50, p90, p10);
    }

    /**
     * 天文层：地外辐照（无不确定性，纯日地几何）。
     * 简化实现：按太阳高度角正弦近似，夏至日正午广州约 1200-1360 W/m²。
     * public 以便单测直接校验物理边界。
     */
    public double extraterrestrialIrradiance(LocalDateTime time) {
        int dayOfYear = time.getDayOfYear();
        double declination = 23.45 * Math.sin(Math.toRadians(360.0 * (284 + dayOfYear) / 365.0));
        double hourAngle = (time.getHour() + time.getMinute() / 60.0 - 12.0) * 15.0;
        double elevation = Math.asin(
                Math.sin(Math.toRadians(latitude)) * Math.sin(Math.toRadians(declination))
                        + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(declination))
                        * Math.cos(Math.toRadians(hourAngle)));
        double eccentricity = 1 + 0.033 * Math.cos(Math.toRadians(360.0 * dayOfYear / 365.0));
        return Math.max(0, G_SC * eccentricity * Math.sin(elevation));
    }

    /** 最优倾角近似等于当地纬度 */
    private double optimalTilt() {
        return latitude;
    }

    /** 逆变器效率随负载率变化：轻载低效，半载峰值 */
    double inverterEfficiency(double pDc) {
        double loadRate = Math.min(1.0, pDc / capacityKwp);
        if (loadRate < 0.1) {
            return 0.92;
        } else if (loadRate < 0.6) {
            return 0.97;
        }
        return 0.96;
    }
}
