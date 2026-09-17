package com.openvpp.assessment.pv;

/**
 * 天气分类 —— 光伏预测不确定性的主开关。
 * 云量系数 f_cloud 直接决定地面辐照衰减，置信度决定申报胆量：
 * 天气越好，预测越准、区间越窄、越敢报量（第 04 篇"天气自适应报价"的代码化）。
 */
public enum WeatherClass {

    /** 完全晴天：云量系数 0.95-1.00，RMSE ~5% */
    CLEAR(0.97, 0.90),

    /** 晴间多云：云量系数 0.70-0.90，RMSE ~15% */
    PARTLY_CLOUDY(0.80, 0.70),

    /** 多云/阴：云量系数 0.15-0.70，RMSE ~20% */
    OVERCAST(0.40, 0.50),

    /** 极端天气（雷暴/台风预警）：云量系数 <0.15，基本不报量 */
    STORM(0.10, 0.30);

    private final double cloudFactor;
    private final double confidence;

    WeatherClass(double cloudFactor, double confidence) {
        this.cloudFactor = cloudFactor;
        this.confidence = confidence;
    }

    public double getCloudFactor() {
        return cloudFactor;
    }

    public double getConfidence() {
        return confidence;
    }
}
