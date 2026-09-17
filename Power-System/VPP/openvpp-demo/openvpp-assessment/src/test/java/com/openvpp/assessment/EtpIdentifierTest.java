package com.openvpp.assessment;

import com.openvpp.assessment.identify.EtpIdentifier;
import com.openvpp.assessment.identify.TempPoint;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 第 35 篇 ETP 参数辨识单测：停机漂移段最小二乘反推 τ / R / C。
 * 合成数据按解析解 T(t)=T_out+(T0-T_out)·e^(-t/τ) 生成，数值可复算。
 */
class EtpIdentifierTest {

    private final EtpIdentifier identifier = new EtpIdentifier();

    /** 合成制冷季停机漂移段：室内 24°C 漂向室外 35°C，τ 已知 */
    private static List<TempPoint> driftSegment(double tauSeconds, double t0, double tOut,
                                                double... elapsedSeconds) {
        List<TempPoint> segment = new ArrayList<>();
        for (double t : elapsedSeconds) {
            double temp = tOut + (t0 - tOut) * Math.exp(-t / tauSeconds);
            segment.add(new TempPoint(t, temp, tOut));
        }
        return segment;
    }

    @Test
    void 最小二乘反推时间常数τ() {
        // 合成算例：T0=24°C，T_out=35°C，τ=3600s
        List<TempPoint> segment = driftSegment(3600, 24.0, 35.0,
                0, 600, 1200, 1800, 2400, 3600);
        double tau = identifier.identifyTau(segment);
        assertEquals(3600.0, tau, 36.0, "辨识 τ 应收敛到合成真值 1% 以内: " + tau);
    }

    @Test
    void 近稳态噪声点被剔除不影响拟合() {
        List<TempPoint> segment = driftSegment(3600, 24.0, 35.0,
                0, 600, 1200, 1800, 2400, 3600);
        // 漂移 20000s 后温度 35.004°C，与室外温差 0.004°C < EPS，必须被剔除
        segment.add(new TempPoint(20000, 35.004, 35.0));
        double tau = identifier.identifyTau(segment);
        assertEquals(3600.0, tau, 36.0, "近稳态噪声点不应污染拟合: " + tau);
    }

    @Test
    void 漂移方向异常拒绝辨识() {
        // 温度远离室外温度（非一阶惯性收敛），斜率 ≥0 必须拒绝
        List<TempPoint> bad = List.of(
                new TempPoint(0, 36.0, 35.0),
                new TempPoint(600, 37.0, 35.0),
                new TempPoint(1200, 38.0, 35.0));
        assertThrows(IllegalStateException.class, () -> identifier.identifyTau(bad),
                "漂移方向背离稳态不是一阶停机段");
    }

    @Test
    void 有效样本不足拒绝辨识() {
        assertThrows(IllegalArgumentException.class,
                () -> identifier.identifyTau(List.of(new TempPoint(0, 24.0, 35.0))),
                "单点无法拟合");
        // 全部落在近稳态噪声区
        List<TempPoint> allNoise = List.of(
                new TempPoint(0, 35.01, 35.0),
                new TempPoint(600, 35.02, 35.0));
        assertThrows(IllegalArgumentException.class, () -> identifier.identifyTau(allNoise),
                "剔除噪声点后有效样本不足必须拒绝");
    }

    @Test
    void 稳态功耗反推热阻R再定热容C() {
        // 稳态热平衡：R = ΔT / Q_net = 11°C / 11kW = 1.0 °C/kW
        double r = identifier.identifyR(11.0, 11.0);
        assertEquals(1.0, r, 1e-9);
        // C = τ / R = 3600s / 1.0 = 3600 kJ/°C（kW=kJ/s，τ=R·C 量纲守恒）
        double c = identifier.identifyC(3600.0, r);
        assertEquals(3600.0, c, 0.01);
        // 交叉校验第 13 篇公式 t = C·ΔT/Q_net：C=3600、ΔT=3°C、Q_net=1.5kW → 7200s
        assertEquals(7200.0, c * 3.0 / 1.5, 0.01, "辨识参数代回 ETP 公式须量纲自洽");
    }

    @Test
    void 热阻热容参数校验() {
        assertThrows(IllegalArgumentException.class, () -> identifier.identifyR(11.0, 0));
        assertThrows(IllegalArgumentException.class, () -> identifier.identifyC(3600, -1));
    }
}
