package com.openvpp.settlement.regional;

/**
 * 江苏调频出清排序键计算器 —— 专栏第 34 篇「排序键里的规则矛盾」。
 *
 * 排序键 = 七日综合调频性能指标 / 调频报价
 *
 * 规则矛盾：
 * <ul>
 *   <li>VPP 报量不报价 → 分母代入市场最高成交价 PM → 同性能下排序靠后</li>
 *   <li>江苏口径 K = K1·K2，缺响应时间 K3 → 储能毫秒级响应优势被抹平</li>
 * </ul>
 *
 * 工程启示：江苏调频对 VPP 的收益预期不能拉高——能控制的只有 K 值本身和投运率，
 * 排序规则是博弈对象，不是博弈筹码。
 *
 * 参数化设计：k3 保留为可选入参，规则修订时改配置不改逻辑——
 * 区域规则永远做成数据，不做成逻辑。
 */
public final class JiangsuFreqRanking {

    /**
     * 计算江苏出清排序键。
     *
     * @param k1             调节速率指标
     * @param k2             调节精度指标
     * @param k3             响应时间指标（江苏现行规则不使用，保留用于规则修订后的扩展）
     * @param bidPrice       调频报价（元/MW）；null 表示报量不报价，分母代入最高成交价
     * @param marketMaxPrice 市场最高成交价 PM（元/MW）
     * @return 排序键 = (K1·K2) / 分母；K3 不参与江苏现行计算
     */
    public static double rankingKey(double k1, double k2, Double k3,
                                    Double bidPrice, double marketMaxPrice) {
        // 江苏现行口径：K = K1·K2，不含 K3
        double performance = k1 * k2;
        // 报量不报价者分母代入最高成交价
        double denominator = bidPrice != null ? bidPrice : marketMaxPrice;
        if (denominator <= 0) {
            throw new IllegalArgumentException("排序键分母（报价或最高成交价）必须为正: " + denominator);
        }
        return performance / denominator;
    }

    /**
     * 完整三要素性能指标（对照用）：K = K1·K2·K3。
     * 江苏规则若修订为完整口径，储能优势将放大 100 倍以上。
     */
    public static double fullPerformance(double k1, double k2, double k3) {
        return k1 * k2 * k3;
    }
}
