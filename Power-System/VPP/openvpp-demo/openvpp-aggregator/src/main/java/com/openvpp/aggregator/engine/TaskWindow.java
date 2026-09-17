package com.openvpp.aggregator.engine;

/**
 * 任务时间窗 —— 容量预占与冲突判定的最小粒度。
 * 教学实现用 epoch 秒闭区间表达，重叠判定用标准区间相交。
 *
 * 生产扩展：真实调度指令还携带执行斜率、确认截止时刻等字段，
 * 此处只保留冲突判定必需的起止时间。
 */
public final class TaskWindow {

    private final long startEpochSec;
    private final long endEpochSec;

    public TaskWindow(long startEpochSec, long durationSec) {
        if (durationSec <= 0) {
            throw new IllegalArgumentException("任务持续时间必须为正: " + durationSec);
        }
        this.startEpochSec = startEpochSec;
        this.endEpochSec = startEpochSec + durationSec;
    }

    /** 闭区间相交判定：两窗口存在公共时刻即视为重叠 */
    public boolean overlaps(TaskWindow other) {
        return this.startEpochSec <= other.endEpochSec
                && other.startEpochSec <= this.endEpochSec;
    }

    public long getStartEpochSec() { return startEpochSec; }
    public long getEndEpochSec() { return endEpochSec; }
    public long getDurationSec() { return endEpochSec - startEpochSec; }
}
