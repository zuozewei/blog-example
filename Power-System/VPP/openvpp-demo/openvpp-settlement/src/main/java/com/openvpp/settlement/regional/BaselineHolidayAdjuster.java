package com.openvpp.settlement.regional;

/**
 * 基线节假日修正 —— 专栏第 34 篇「方法 A+B 组合」教学实现。
 *
 * 修正三法：
 * <ul>
 *   <li>方法 A：日类型过滤——合格日只选与事件日相同日类型（工作日/周末/节假日各自成池）</li>
 *   <li>方法 B：天气调整——BLadj(t) = BL(t) · f(Temp_event) / f(Temp_baseline)</li>
 *   <li>方法 C：上同周调整——取去年同期同周同日类型负荷（只宜交叉校验，不宜单独使用）</li>
 * </ul>
 *
 * 国内通行做法：A+B 组合——先按日类型过滤样本池，再对过滤后的基线做天气调整。
 * 方法 C 在负荷有年增长趋势的工商业场景会系统性偏低。
 *
 * 合规红线：防基线操纵是平台义务——基线抬升、虚假响应、响应甩尾三类手法中，
 * 基线抬升的检测最机械（监控非响应日用电是否相对自身历史异常偏高）。
 *
 * 教学简化：方法 A 的样本池过滤假设已由上游完成，本类只承载方法 B 的温度系数调整；
 * 温度-负荷灵敏度函数 f(T) 按楼宇历史回归，此处以线性近似教学。
 */
public final class BaselineHolidayAdjuster {

    /**
     * 温度-负荷灵敏度函数（教学线性近似）。
     * 实际应按楼宇历史数据回归：夏季温度每升 1°C，负荷约增 3%~8%。
     *
     * @param tempCelsius 温度（°C）
     * @return 灵敏度系数（无量纲）
     */
    public static double tempLoadSensitivity(double tempCelsius) {
        // 教学线性近似：以 25°C 为基准，每度 5% 灵敏度
        return 1.0 + 0.05 * (tempCelsius - 25.0);
    }

    /**
     * 方法 B：天气调整——对基线负荷按温度比缩放。
     *
     * @param baselineKw    原始基线负荷（kW，方法 A 过滤后的样本池已算得）
     * @param eventTemp     事件日温度（°C）
     * @param baselineTemp  基线样本平均温度（°C）
     * @return 调整后基线（kW）
     */
    public static double adjustByWeather(double baselineKw, double eventTemp, double baselineTemp) {
        double fEvent = tempLoadSensitivity(eventTemp);
        double fBaseline = tempLoadSensitivity(baselineTemp);
        if (fBaseline <= 0) {
            throw new IllegalArgumentException("基线温度灵敏度必须为正，检查回归参数");
        }
        return baselineKw * fEvent / fBaseline;
    }
}
