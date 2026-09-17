package com.openvpp.aggregator.engine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 容量预占台账 —— 防止同一资源在重叠时间窗内被重复承接。
 *
 * 机制：每次分解成功后登记 (resourceId, window, occupiedKw)；
 * 后续任务先查该资源在重叠窗口内的累计占用，
 * 剩余可用 = 有效能力 - 重叠窗口累计占用，剩余为 0 即不再分配。
 *
 * 教学实现为进程内内存表，线程不安全、重启即失；
 * 生产扩展见第 14 篇文末：预占持久化（库表/Redis）、并发一致性（行锁/乐观锁）、
 * 跨时间窗占用的滚动释放。
 */
public class CapacityReservationLedger {

    private static final class Entry {
        private final TaskWindow window;
        private final BigDecimal occupiedKw;

        private Entry(TaskWindow window, BigDecimal occupiedKw) {
            this.window = window;
            this.occupiedKw = occupiedKw;
        }
    }

    private final Map<String, List<Entry>> reservations = new HashMap<>();

    /**
     * 登记一次预占。
     */
    public void reserve(String resourceId, TaskWindow window, BigDecimal occupiedKw) {
        reservations.computeIfAbsent(resourceId, k -> new ArrayList<>())
                .add(new Entry(window, occupiedKw));
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
     * 释放某资源已登记的全部预占（资源掉线重估、任务撤销时调用）。
     */
    public void releaseAll(String resourceId) {
        reservations.remove(resourceId);
    }
}
