package com.openvpp.aggregator.engine;

import java.math.BigDecimal;

/**
 * 已评估资源 —— 聚合引擎的输入单元。
 * 由"档案（resource 模块）+ 能力评估结果（assessment 模块）"合成，
 * 聚合层不重复读档案、不重复算评估，只消费两者的合成快照。
 *
 * 能力模型字段（方向 / 时间窗 / 数据新鲜度）：
 * 教学实现只建模型并注明消费位置，评估侧的折算算法是第 12/13 篇的职责；
 * 聚合侧假定 credibleCapacityKw 已经是对任务方向、时间窗、数据时效折算后的有效能力。
 */
public class AssessedResource {

    /** 调节方向：UP=上调（增出力/降负荷），DOWN=下调 */
    public enum Direction {
        UP, DOWN
    }

    private final String resourceId;
    private final String clearingNodeId;
    private final BigDecimal adjustCapacityKw;
    private final BigDecimal confidence;
    private final long sustainSeconds;
    private final boolean contractValid;

    /** 本次评估适用的调节方向（与任务方向不一致的资源不得参与分配） */
    private final Direction direction;

    /** 能力有效窗口起点（epoch 秒）：任务开始时间早于该值时能力打折 */
    private final long availableFromEpochSec;

    /** 能力数据新鲜度（秒）：评估输出距当前的时间差，超阈值应按陈旧数据处理 */
    private final long dataAgeSeconds;

    /**
     * 兼容构造：教学简化默认 DOWN 方向、即时可用、数据新鲜。
     */
    public AssessedResource(String resourceId, String clearingNodeId,
                            BigDecimal adjustCapacityKw, BigDecimal confidence,
                            long sustainSeconds, boolean contractValid) {
        this(resourceId, clearingNodeId, adjustCapacityKw, confidence,
                sustainSeconds, contractValid, Direction.DOWN, 0L, 0L);
    }

    /**
     * 完整能力模型构造。
     *
     * @param direction            调节方向（任务方向不匹配的资源不参与分配）
     * @param availableFromEpochSec 能力生效时间（任务开始早于该值则有效能力折算为 0）
     * @param dataAgeSeconds       数据年龄（教学实现不参与打分，生产侧超阈值应触发重新评估）
     */
    public AssessedResource(String resourceId, String clearingNodeId,
                            BigDecimal adjustCapacityKw, BigDecimal confidence,
                            long sustainSeconds, boolean contractValid,
                            Direction direction, long availableFromEpochSec,
                            long dataAgeSeconds) {
        this.resourceId = resourceId;
        this.clearingNodeId = clearingNodeId;
        this.adjustCapacityKw = adjustCapacityKw;
        this.confidence = confidence;
        this.sustainSeconds = sustainSeconds;
        this.contractValid = contractValid;
        this.direction = direction;
        this.availableFromEpochSec = availableFromEpochSec;
        this.dataAgeSeconds = dataAgeSeconds;
    }

    /** 置信度折扣后的可信容量 —— 聚合承诺只加可信部分 */
    public BigDecimal credibleCapacityKw() {
        return adjustCapacityKw.multiply(confidence);
    }

    /**
     * 针对一次任务的有效能力：方向匹配且任务开始时能力已生效，
     * 返回可信容量；否则返回 0（该资源对本任务不可用）。
     *
     * 教学假设：持续时间约束仅要求 sustainSeconds 覆盖任务时长，
     * 不做更细的分时功率曲线校验；数据新鲜度字段仅入模型不打分，
     * 生产实现应把"陈旧数据"折算进置信度或直接剔除。
     */
    public BigDecimal effectiveCapacityKw(Direction taskDirection, long taskStartEpochSec,
                                          long taskDurationSec) {
        if (this.direction != taskDirection) {
            return BigDecimal.ZERO;
        }
        if (taskStartEpochSec < availableFromEpochSec) {
            return BigDecimal.ZERO;
        }
        if (sustainSeconds < taskDurationSec) {
            return BigDecimal.ZERO;
        }
        return credibleCapacityKw();
    }

    public String getResourceId() { return resourceId; }
    public String getClearingNodeId() { return clearingNodeId; }
    public BigDecimal getAdjustCapacityKw() { return adjustCapacityKw; }
    public BigDecimal getConfidence() { return confidence; }
    public long getSustainSeconds() { return sustainSeconds; }
    public boolean isContractValid() { return contractValid; }
    public Direction getDirection() { return direction; }
    public long getAvailableFromEpochSec() { return availableFromEpochSec; }
    public long getDataAgeSeconds() { return dataAgeSeconds; }
}
