package com.openvpp.aggregator.engine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 容量预占台账 —— 防止同一资源在重叠时间窗内被重复承接。
 *
 * 机制：每次分解成功后登记 (taskId, resourceId, window, occupiedKw)；
 * 后续任务先查该资源在重叠窗口内的累计占用，
 * 剩余可用 = 有效能力 - 重叠窗口累计占用，剩余为 0 即不再分配。
 * 预占生命周期：任务完成/取消/失败时按 taskId 释放（releaseByTask）；
 * 演示重置时整体清空（clear），避免内存台账与库表脱节。
 *
 * 教学实现为进程内内存表，线程不安全、重启即失；
 * 生产扩展见第 14 篇文末：预占持久化（库表/Redis）、并发一致性（行锁/乐观锁）、
 * 跨时间窗占用的滚动释放。
 */
public class CapacityReservationLedger {

    private static final class Entry {
        private final String taskId;
        private final TaskWindow window;
        private final BigDecimal occupiedKw;

        private Entry(String taskId, TaskWindow window, BigDecimal occupiedKw) {
            this.taskId = taskId;
            this.window = window;
            this.occupiedKw = occupiedKw;
        }
    }

    private final Map<String, List<Entry>> reservations = new HashMap<>();

    /**
     * 登记一次预占（taskId 贯穿响应任务，任务完成/取消/失败时按此释放）。
     */
    public void reserve(String taskId, String resourceId, TaskWindow window, BigDecimal occupiedKw) {
        reservations.computeIfAbsent(resourceId, k -> new ArrayList<>())
                .add(new Entry(taskId, window, occupiedKw));
    }

    /**
     * 某资源在与 window 重叠的所有已登记任务中的累计占用（kW）。
     */
    public BigDecimal occupiedIn(String resourceId, TaskWindow window) {
        return reservations.getOrDefault(resourceId, List.of()).stream()
                .filter(e -> e.window.overlaps(window))
                .map(e -> e.occupiedKw)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 剩余可用 = 有效能力 - 重叠窗口累计占用，不为负。
     */
    public BigDecimal remainingKw(AssessedResource resource,
                                  AssessedResource.Direction direction,
                                  TaskWindow window) {
        BigDecimal effective = resource.effectiveCapacityKw(
                direction, window.getStartEpochSec(), window.getDurationSec());
        BigDecimal remaining = effective.subtract(occupiedIn(resource.getResourceId(), window));
        return remaining.signum() < 0 ? BigDecimal.ZERO : remaining;
    }

    /**
     * 释放某资源已登记的全部预占（资源掉线重估时调用）。
     */
    public void releaseAll(String resourceId) {
        reservations.remove(resourceId);
    }

    /**
     * 按任务标识释放预占（任务完成/取消/失败时调用）：
     * 遍历各资源移除该 taskId 的登记项，空资源的整条记录一并清除。
     * 返回实际释放的登记条数（0 表示该任务无预占，调用方可据此告警）。
     */
    public int releaseByTask(String taskId) {
        int released = 0;
        for (List<Entry> entries : reservations.values()) {
            for (int i = entries.size() - 1; i >= 0; i--) {
                if (entries.get(i).taskId.equals(taskId)) {
                    entries.remove(i);
                    released++;
                }
            }
        }
        reservations.values().removeIf(List::isEmpty);
        return released;
    }

    /**
     * 清空全部预占（演示重置用，与数据库 deleteAll 配对调用）。
     */
    public void clear() {
        reservations.clear();
    }
}
