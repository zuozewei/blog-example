package com.openvpp.aggregator.engine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 指令分解器 —— 把单元级调度指令按成员有效能力等比分解到单资源。
 *
 * 修复历史缺陷（第 14 篇缺陷复盘）：旧实现用"折扣后的聚合容量"作分母、
 * 却用"折扣前的成员可信容量"作权重，比例之和高估（1200/1080），
 * 再把负差额压给排序末位成员，存在负分配风险。
 *
 * 修复原则：
 * 1. 两个口径分离 —— 聚合可承诺容量（折扣后）只用于"任务可行性校验"，
 *    分配权重的分母一律用"成员有效能力之和"（折扣前、未打聚合折扣）；
 * 2. 先可行性校验再分配 —— 任务量 > 可承诺容量、或成员有效能力之和不敷时，
 *    明确返回缺口量，不得静默截断、不得硬凑；
 * 3. 舍入残差只分配给仍有可用余量的成员 —— 不突破任何成员的有效能力上界；
 * 4. 容量预占 —— 同一资源在重叠时间窗内的任务占用累计，防止重复承接。
 */
public class InstructionDecomposer {

    /**
     * 分解结果。
     * 可行：plan 非空且 gapKw 为 0；
     * 不可行：plan 为空，gapKw 为缺口量（调用方必须显式处理，不得当作分配方案使用）。
     */
    public static final class DecompositionResult {
        private final Map<String, BigDecimal> plan;
        private final BigDecimal gapKw;

        private DecompositionResult(Map<String, BigDecimal> plan, BigDecimal gapKw) {
            this.plan = plan;
            this.gapKw = gapKw;
        }

        static DecompositionResult infeasible(BigDecimal gapKw) {
            return new DecompositionResult(Map.of(), gapKw);
        }

        /** 构造可行结果（恢复路径沿用既有预占时调用方需要） */
        public static DecompositionResult feasible(Map<String, BigDecimal> plan) {
            return new DecompositionResult(plan, BigDecimal.ZERO);
        }

        /** 任务可完整承接：分配方案可用且总和守恒等于指令量 */
        public boolean isFeasible() {
            return gapKw.signum() == 0;
        }

        /** resourceId → 分解指令（kW）；不可行时为空 Map */
        public Map<String, BigDecimal> getPlan() {
            return plan;
        }

        /** 缺口量（kW）；可行时为 0 */
        public BigDecimal getGapKw() {
            return gapKw;
        }
    }

    /**
     * 分解单元指令（含方向、时间窗与容量预占）。
     *
     * @param members           单元内成员
     * @param committablePoolKw 聚合可承诺容量（折扣后，仅用于可行性校验，不作分配分母）
     * @param commandKw         单元级调度指令（下调为正）
     * @param direction         任务调节方向（与成员方向不匹配的不参与分配）
     * @param window            任务时间窗（预占冲突判定粒度）
     * @param taskId            响应任务标识（预占按此登记，任务结束按此释放）
     * @param ledger            容量预占台账（重叠窗口累计占用从有效能力中扣除）
     * @return 分解结果；不可行时返回缺口量
     */
    public DecompositionResult decompose(List<AssessedResource> members,
                                         BigDecimal committablePoolKw,
                                         BigDecimal commandKw,
                                         AssessedResource.Direction direction,
                                         TaskWindow window,
                                         String taskId,
                                         CapacityReservationLedger ledger) {
        if (members == null || members.isEmpty()) {
            throw new IllegalArgumentException("成员列表为空，无法分解");
        }
        if (committablePoolKw == null || committablePoolKw.signum() <= 0) {
            throw new IllegalArgumentException("可承诺容量必须为正: " + committablePoolKw);
        }
        if (commandKw == null || commandKw.signum() <= 0) {
            throw new IllegalArgumentException("指令量必须为正: " + commandKw);
        }

        // 防线 1（可行性校验）：任务量不得超过聚合可承诺容量
        if (commandKw.compareTo(committablePoolKw) > 0) {
            return DecompositionResult.infeasible(commandKw.subtract(committablePoolKw));
        }

        // 逐成员计算剩余可用能力 = 有效能力（方向/时间窗匹配）- 重叠窗口已预占
        Map<String, BigDecimal> remainingByMember = new LinkedHashMap<>();
        for (AssessedResource member : members) {
            BigDecimal remaining = ledger.remainingKw(member, direction, window);
            if (remaining.signum() > 0) {
                remainingByMember.put(member.getResourceId(), remaining);
            }
        }
        BigDecimal effectiveSum = remainingByMember.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 防线 2（可行性重估）：有效能力之和不敷（资源掉线/预占冲突）时返回缺口
        if (effectiveSum.compareTo(commandKw) < 0) {
            return DecompositionResult.infeasible(commandKw.subtract(effectiveSum));
        }

        // 等比分配：权重分母 = 成员剩余有效能力之和（与可承诺容量是两个口径）
        Map<String, BigDecimal> plan = new LinkedHashMap<>();
        BigDecimal allocated = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> e : remainingByMember.entrySet()) {
            BigDecimal share = commandKw
                    .multiply(e.getValue())
                    .divide(effectiveSum, 3, RoundingMode.DOWN);
            plan.put(e.getKey(), share);
            allocated = allocated.add(share);
        }

        // 防线 3（舍入残差）：只分配给仍有可用余量的成员，不突破有效能力上界。
        // 因 Σ 剩余能力 ≥ 指令量且 Σ 分配 ≤ 指令量，Σ 余量 ≥ 残差恒成立，残差必能分完。
        BigDecimal residue = commandKw.subtract(allocated);
        if (residue.signum() > 0) {
            List<String> byRemainingDesc = new ArrayList<>(remainingByMember.keySet());
            byRemainingDesc.sort(Comparator.comparing(remainingByMember::get).reversed());
            for (String resourceId : byRemainingDesc) {
                if (residue.signum() <= 0) {
                    break;
                }
                BigDecimal headroom = remainingByMember.get(resourceId)
                        .subtract(plan.get(resourceId));
                if (headroom.signum() <= 0) {
                    continue;
                }
                BigDecimal topUp = residue.min(headroom);
                plan.merge(resourceId, topUp, BigDecimal::add);
                residue = residue.subtract(topUp);
            }
        }

        // 防线 4（容量预占）：按任务标识登记本次占用，
        // 重叠时间窗的后续任务将看到剩余能力；任务结束由编排器按 taskId 释放
        plan.forEach((resourceId, share) -> {
            if (share.signum() > 0) {
                ledger.reserve(taskId, resourceId, window, share);
            }
        });
        return DecompositionResult.feasible(plan);
    }

    /**
     * 教学便利入口：默认下调方向、固定 1 小时窗口、独立空台账（无历史预占）。
     */
    public DecompositionResult decompose(List<AssessedResource> members,
                                         BigDecimal committablePoolKw,
                                         BigDecimal commandKw) {
        return decompose(members, committablePoolKw, commandKw,
                AssessedResource.Direction.DOWN, new TaskWindow(0L, 3600L),
                "teaching-standalone", new CapacityReservationLedger());
    }
}
