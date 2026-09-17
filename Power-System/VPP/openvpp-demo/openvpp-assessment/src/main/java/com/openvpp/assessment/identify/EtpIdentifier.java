package com.openvpp.assessment.identify;

import java.util.ArrayList;
import java.util.List;

/**
 * ETP 参数辨识器 —— 从「停机-温度漂移」段反推一阶惯性时间常数 τ = R·C。
 *
 * 物理模型（专栏第 13/35 篇）：空调关停后室内温度按一阶惯性漂移，
 * 解析解为指数曲线 T(t) = T_out + (T₀ - T_out)·e^(-t/τ)。
 *
 * 工程洞察：辨识不需要专门做实验——每次停机后的温度漂移都是免费的阶跃响应，
 * 日常运行日志就是辨识样本。样本要挑「完整停机段」：停机前有稳态、漂移时长足够。
 *
 * 教学简化：离线最小二乘已覆盖 80% 场景；卡尔曼滤波在线递推为进阶选项（第 35 篇）。
 */
public final class EtpIdentifier {

    /** 剔除接近稳态的噪声点阈值：|T - T_out| 小于此值不参与拟合 */
    private static final double EPS = 0.05;

    /**
     * 从一次「停机-温度漂移」段辨识时间常数 τ = R·C（秒）。
     *
     * 线性化：ln(T - T_out) = ln(T₀ - T_out) - t/τ → 对线性段做最小二乘，斜率 = -1/τ。
     *
     * @param driftSegment 停机漂移段采样点（按时间升序）
     * @return 时间常数 τ（秒）
     * @throws IllegalArgumentException 有效样本不足或数据不满足一阶惯性假设
     */
    public double identifyTau(List<TempPoint> driftSegment) {
        if (driftSegment == null || driftSegment.size() < 2) {
            throw new IllegalArgumentException("停机漂移段至少需要 2 个采样点");
        }
        List<double[]> linearized = new ArrayList<>();
        for (TempPoint p : driftSegment) {
            double delta = p.temp() - p.outdoorTemp();
            // 剔除接近稳态的噪声点
            if (Math.abs(delta) < EPS) {
                continue;
            }
            linearized.add(new double[]{p.elapsedSeconds(), Math.log(Math.abs(delta))});
        }
        if (linearized.size() < 2) {
            throw new IllegalArgumentException("有效漂移样本不足（剔除稳态噪声点后少于 2 个）");
        }
        double slope = LeastSquares.fit(linearized).slope();
        if (slope >= 0) {
            throw new IllegalStateException(
                    "漂移段温度应单调收敛于室外温度，斜率必须<0，实测 slope=" + slope);
        }
        return -1.0 / slope;
    }

    /**
     * 由停机前稳态功耗反推等效热阻 R。
     *
     * 稳态热平衡：Q_net = (T_in - T_out) / R → R = ΔT / Q_net
     *
     * @param steadyTempDiff 停机前稳态内外温差（°C）
     * @param netHeatGainKw  稳态净得热（kW = kJ/s）
     * @return 等效热阻 R（°C/kW = °C·s/kJ）
     */
    public double identifyR(double steadyTempDiff, double netHeatGainKw) {
        if (netHeatGainKw <= 0) {
            throw new IllegalArgumentException("净得热必须为正: " + netHeatGainKw);
        }
        return steadyTempDiff / netHeatGainKw;
    }

    /**
     * 由 τ 和 R 计算等效热容 C = τ / R。
     *
     * @param tauSeconds 时间常数（秒）
     * @param rDegPerKw  等效热阻（°C/kW）
     * @return 等效热容 C（kJ/°C）
     */
    public double identifyC(double tauSeconds, double rDegPerKw) {
        if (rDegPerKw <= 0) {
            throw new IllegalArgumentException("热阻必须为正: " + rDegPerKw);
        }
        return tauSeconds / rDegPerKw;
    }
}
