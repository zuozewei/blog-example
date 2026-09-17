package com.openvpp.assessment;

import com.openvpp.assessment.pv.ForecastCurve;
import com.openvpp.assessment.pv.PvPowerForecaster;
import com.openvpp.assessment.pv.WeatherClass;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 光伏出力预测单测：天文层精度 / 温度损耗 / 天气置信度 / 区间宽度。
 * 基准算例：5MWp 屋顶光伏，广州（北纬 23.13°），夏至日正午晴天。
 */
class PvPowerForecasterTest {

    private static final double CAPACITY_KWP = 5000.0;
    private static final double GUANGZHOU_LAT = 23.13;

    private final PvPowerForecaster forecaster =
            new PvPowerForecaster(CAPACITY_KWP, GUANGZHOU_LAT, 20.0, -0.35);

    private static final LocalDateTime SUMMER_NOON =
            LocalDateTime.of(2026, 6, 21, 12, 0);

    @Test
    void 天文层夏至正午辐照在合理区间() {
        double gEx = forecaster.extraterrestrialIrradiance(SUMMER_NOON);
        // 广州夏至正午地外辐照约 1200-1360 W/m²（素材算例 1320 为近似值）
        assertTrue(gEx > 1100 && gEx < 1400,
                "天文层辐照超物理边界: " + gEx);
    }

    @Test
    void 晴天正午并网功率约为铭牌75到85折() {
        ForecastCurve curve = forecaster.forecast(SUMMER_NOON, WeatherClass.CLEAR, 35.0);
        double ratio = curve.getP50Kw() / CAPACITY_KWP;
        // 素材结论：5MWp 晴天正午实际并网约 3800-4000kW（0.76-0.80）
        assertTrue(ratio > 0.70 && ratio < 0.85,
                "温度损耗+逆变效率后功率比异常: " + ratio);
        assertTrue(curve.getP50Kw() < CAPACITY_KWP, "正午功率不应达铭牌（热损耗）");
    }

    @Test
    void 天气越差出力越低且置信度越低() {
        ForecastCurve clear = forecaster.forecast(SUMMER_NOON, WeatherClass.CLEAR, 35.0);
        ForecastCurve cloudy = forecaster.forecast(SUMMER_NOON, WeatherClass.PARTLY_CLOUDY, 35.0);
        ForecastCurve storm = forecaster.forecast(SUMMER_NOON, WeatherClass.STORM, 35.0);

        assertTrue(clear.getP50Kw() > cloudy.getP50Kw());
        assertTrue(cloudy.getP50Kw() > storm.getP50Kw());
        assertEquals(0.90, WeatherClass.CLEAR.getConfidence());
        assertEquals(0.30, WeatherClass.STORM.getConfidence());
    }

    @Test
    void 天气越差预测区间越宽() {
        ForecastCurve clear = forecaster.forecast(SUMMER_NOON, WeatherClass.CLEAR, 35.0);
        ForecastCurve overcast = forecaster.forecast(SUMMER_NOON, WeatherClass.OVERCAST, 35.0);

        assertTrue(overcast.uncertaintyWidth() > clear.uncertaintyWidth(),
                "坏天气的不确定性区间必须更宽");
        assertTrue(clear.getP90Kw() < clear.getP50Kw());
        assertTrue(clear.getP10Kw() > clear.getP50Kw());
    }

    @Test
    void 凌晨出力为零() {
        LocalDateTime midnight = LocalDateTime.of(2026, 6, 21, 2, 0);
        ForecastCurve curve = forecaster.forecast(midnight, WeatherClass.CLEAR, 25.0);
        assertEquals(0.0, curve.getP50Kw(), 0.01, "夜间出力必须为零");
    }
}
