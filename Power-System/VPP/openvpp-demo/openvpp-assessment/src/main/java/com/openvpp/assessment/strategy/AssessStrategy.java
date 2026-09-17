package com.openvpp.assessment.strategy;

import com.openvpp.assessment.capability.ResourceCapability;
import com.openvpp.common.enums.Scenario;

/**
 * 评估策略 —— 对应 GB/T 44260 综合评估（附录 A 加权评分法）。
 * 调峰场景：调节容量权重高，响应时间权重低；
 * 调频场景：响应时间、偏差率权重高；
 * 备用场景：持续时间、调节容量权重高。
 * 权重是运营 know-how，标准不给，做成配置项不写死。
 */
public interface AssessStrategy {

    /**
     * 按场景计算单项指标得分
     *
     * @param capability 资源能力评估结果（七指标 + 置信度）
     * @param scenario   目标应用场景
     * @return 0-100 加权综合得分
     */
    double score(ResourceCapability capability, Scenario scenario);
}
