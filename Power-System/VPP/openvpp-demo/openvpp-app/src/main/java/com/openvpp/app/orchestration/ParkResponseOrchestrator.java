package com.openvpp.app.orchestration;

import com.openvpp.aggregator.engine.AssessedResource;
import com.openvpp.aggregator.engine.CapacityPoolCalculator;
import com.openvpp.aggregator.engine.CapacityReservationLedger;
import com.openvpp.aggregator.engine.InstructionDecomposer;
import com.openvpp.aggregator.engine.TaskWindow;
import com.openvpp.app.persistence.ResponseRepository;
import com.openvpp.dispatch.instruction.DispatchInstruction;
import com.openvpp.dispatch.instruction.InstructionRepository;
import com.openvpp.dispatch.instruction.InstructionService;
import com.openvpp.settlement.allocation.ProfitAllocator;
import com.openvpp.settlement.assessment.DeviationAssessor;
import com.openvpp.settlement.baseline.BaselineCalculator;
import com.openvpp.settlement.baseline.BaselineRule;
import com.openvpp.settlement.baseline.MeteringResult;
import com.openvpp.settlement.baseline.ResponseMetering;
import com.openvpp.settlement.baseline.SamplePoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 园区需求响应编排服务 —— 把独立模块串成完整业务闭环。
 *
 * 链路：模拟遥测 → 接入校验 → 数据入库 → 能力评估 → 资源聚合 → 响应任务 →
 *       指令下发 → 执行核验 → 响应量计算 → 结算分摊 → 账单查询。
 *
 * 教学假设（全部数值为教学设定，不对应任何地区准入或结算规则）：
 *   响应窗口 14:00—15:00；15 分钟粒度 4 时段；各时段基线 1000 kW、实测 400 kW；
 *   响应电量 600×1h = 600 kWh；补偿单价 2 元/kWh；平台实收 1200 元；
 *   保底 50%（600 元）按申报容量占比分配；平台服务费对扣除保底后的剩余部分收 10%
 *   （600×10% = 60 元）；用户可分配 = 保底 600 + 分成池 540 = 1140 元。
 *   （与 PARK-DEMO.md 手工核算底稿第四节逐项一致）
 *
 * 数据口径：遥测/计量/容量均为内置模拟源（buildSamples/buildMembers 直接构造），
 *       不来自网关真实接入；默认 openvpp.gateway.mode=local，零外部依赖。
 *
 * 幂等三态：任务状态 DISPATCHED（处理中）/ SETTLED（已结算）/ GAP（不可行）。
 *   同一 responseId 重复触发：SETTLED 直接返回不重复出账；DISPATCHED 不重复
 *   下发指令，但继续执行结算——修复"实收先写、分摊后写、中途异常后重跑被误判
 *   幂等、分摊永远为 0"的缺陷：检测到实收已写而分摊缺失时走恢复路径补齐，
 *   分摊补齐全量替换（先删后写）保证金额不重复、可修半成品，最终做资金守恒核对。
 *
 * 争议路径（DISPUTED）：结算完成后检测到计量补到数据（教学模拟第 2 时段
 *   实测修正 400→380 kW），按更正口径重算响应量与金额，生成版本化更正账单
 *   （CORRECTION + memo 携带账期版本号），原始账单保留不删，可追溯可冲正。
 *
 * 预占生命周期：指令分解登记容量预占（内存台账，按 responseId 标识）；
 *   任务正常完成后释放、取消/失败按任务标识释放、演示重置（resetRuntimeState）
 *   整体清空，避免内存台账与库表脱节导致重置后再运行误报缺口。
 */
@Service
public class ParkResponseOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ParkResponseOrchestrator.class);

    // ---- 教学假设常量（与文章手工核算底稿一一对应） ----
    public static final double BASELINE_KW = 1000.0;
    public static final double ACTUAL_KW = 400.0;
    /** 争议路径教学设定：计量补到后第 2 时段（point_index=1）的修正实测 */
    public static final double DISPUTED_CORRECTED_KW = 380.0;
    public static final int POINTS = 4;                       // 4 个 15 分钟点
    public static final double PRICE_YUAN_PER_KWH = 2.0;      // 假设补偿单价
    public static final double PLATFORM_CUT_RATE = 0.10;      // 平台服务费 10%
    public static final String RULE_VERSION = "TEACH-2026.1";
    /** 账期版本号：原始出账 V1，更正出账 V2（更正账单 memo 携带，区分账期版本） */
    public static final String BILL_VERSION_ORIGINAL = "V1";
    public static final String BILL_VERSION_CORRECTED = "V2";

    private final ResponseRepository repo;
    private final InstructionService instructionService;
    private final InstructionRepository instructionRepository;
    private final BaselineCalculator baselineCalculator = new BaselineCalculator(BaselineRule.teachingDefault());
    private final ResponseMetering metering = new ResponseMetering();
    private final DeviationAssessor assessor = DeviationAssessor.provincialDefault();
    private final ProfitAllocator allocator = new ProfitAllocator(BigDecimal.valueOf(PLATFORM_CUT_RATE));
    private final InstructionDecomposer decomposer = new InstructionDecomposer();
    private final CapacityPoolCalculator poolCalculator = new CapacityPoolCalculator(0.9);
    private final CapacityReservationLedger reservationLedger = new CapacityReservationLedger();

    public ParkResponseOrchestrator(ResponseRepository repo,
                                    InstructionService instructionService,
                                    InstructionRepository instructionRepository) {
        this.repo = repo;
        this.instructionService = instructionService;
        this.instructionRepository = instructionRepository;
    }

    /**
     * 跑一遍园区需求响应闭环。
     *
     * @param responseId   贯穿案例关联标识（幂等键）
     * @param path         NORMAL / DEGRADED / DISPUTED
     * @param declaredKwh  申报响应电量
     * @param targetKw     调度目标功率（下调）
     */
    @Transactional
    public DemoRunResult run(String responseId, String path,
                             BigDecimal declaredKwh, BigDecimal targetKw) {
        DemoRunResult result = new DemoRunResult();
        result.setResponseId(responseId);
        result.setPath(path);
        List<String> trace = new ArrayList<>();

        // 幂等三态判定（依据结算完成状态，不再只看实收账单是否存在）：
        // SETTLED（已完成）→ 幂等短路；DISPATCHED（处理中/中断残留）→ 恢复路径补齐；
        // 任务不存在 → 全新执行。修复历史缺陷：实收先写、分摊后写，中途异常后
        // 重跑曾被"实收存在"误判幂等，分摊永远为 0。
        String taskState = repo.taskState(responseId);
        if ("SETTLED".equals(taskState)) {
            result.setIdempotentReplay(true);
            trace.add("幂等拦截：responseId=" + responseId + " 已结算（SETTLED），不重复执行/出账，直接返回既有结果");
            log.warn("幂等拦截: {} 已结算，跳过重复执行", responseId);
            result.setTrace(trace);
            return result;
        }
        boolean recovery = "DISPATCHED".equals(taskState)
                && repo.billExists(responseId, "PLATFORM", "SETTLE");
        if (recovery) {
            trace.add("恢复路径：检测到实收已写而任务仍为 DISPATCHED（上次中断残留），"
                    + "不重复下发指令，补齐分摊并完成结算");
            log.warn("恢复路径: {} 实收已写分摊缺失，补齐后继续", responseId);
        }

        // ① 模拟遥测 → 接入校验 → 数据入库（基线样本与实测点）
        // 数据为内置模拟源直接构造（非网关真实接入），链路其余环节走真实业务代码
        trace.add("① 遥测接入（内置模拟源）：采集基线样本与响应期实测，校验通过后入库");
        LocalDate responseDay = LocalDate.now();
        List<SamplePoint> samples = buildSamples(responseDay);

        // ② 能力评估 + ③ 资源聚合
        trace.add("②③ 能力评估+资源聚合：逐成员评估有效能力，聚合成可调容量池");
        List<AssessedResource> members = buildMembers(path);
        BigDecimal committablePool = poolCalculator.poolOf(members);
        trace.add("   聚合可承诺容量（折扣0.9后）= " + committablePool + " kW");

        // ④ 响应任务：先可行性校验
        long nowMs = System.currentTimeMillis();
        TaskWindow window = new TaskWindow(nowMs / 1000, 3600);   // 构造为 (起点秒, 时长秒)
        trace.add("④ 响应任务：任务量 " + targetKw + " kW，先做可行性校验");

        // ⑤ 指令分解（含容量预占）
        // 恢复路径跳过：上次中断时预占可能仍登记在台账，重复分解会重复预占、
        // 挤占剩余能力导致误报缺口；且指令已在途/终态，无需重新分解下发。
        InstructionDecomposer.DecompositionResult decomp;
        if (recovery) {
            decomp = InstructionDecomposer.DecompositionResult.feasible(Map.of());
            trace.add("⑤ 指令分解：恢复路径跳过（沿用在途指令与既有预占）");
        } else {
            decomp = decomposer.decompose(
                    members, committablePool, targetKw, AssessedResource.Direction.DOWN,
                    window, responseId, reservationLedger);
        }
        result.setFeasible(decomp.isFeasible());
        result.setGapKw(decomp.getGapKw());
        repo.saveTask(responseId, "evt-" + responseId, declaredKwh, targetKw,
                nowMs, nowMs + 3600_000,
                decomp.isFeasible() ? "DISPATCHED" : "GAP", decomp.getGapKw());

        if (!decomp.isFeasible()) {
            trace.add("⑤ 指令分解：不可行，缺口 " + decomp.getGapKw() + " kW（降级路径显式报缺口）");
            result.setTrace(trace);
            log.warn("任务不可行: {} 缺口 {}kW", responseId, decomp.getGapKw());
            return result;
        }
        if (!recovery) {
            trace.add("⑤ 指令分解：可行，按成员有效能力等比分配，登记容量预占");
        }

        // ⑥ 指令下发 → ⑦ 执行核验（遥测驱动）
        // 恢复路径跳过下发：指令已在内存仓库（在途或终态），重复 send 会被指令级
        // 幂等拦截，但教学回环的执行核验（onAck/onTelemetry）不重复驱动，
        // 以持久化的指令记录为准。
        if (!recovery) {
            dispatchAndVerify(responseId, decomp.getPlan(), trace);
        } else {
            trace.add("⑥⑦ 指令下发与执行核验：恢复路径跳过（沿用既有指令执行结果）");
        }

        // ⑧ 响应量计算（基线 - 实测，正偏差积分，缺失/零值口径）
        MeteringResult metered = computeResponse(responseId, samples, responseDay, trace);
        result.setBaselineKw(BigDecimal.valueOf(BASELINE_KW));
        result.setActualKw(BigDecimal.valueOf(ACTUAL_KW));
        result.setResponseKwh(BigDecimal.valueOf(metered.responseKwh()).setScale(3, RoundingMode.HALF_UP));

        // ⑨ 结算（合格率 + 偏差考核 + 结算电量）
        BigDecimal settleYuan = settle(responseId, declaredKwh, metered.responseKwh(),
                metered.responseKwh(), trace, result);

        // ⑩ 分摊 + 账单 + 任务完成状态（同事务边界；分摊全量替换保证幂等可补偿）
        Map<String, BigDecimal> allocation = allocateAndBill(responseId, settleYuan,
                BILL_VERSION_ORIGINAL, trace);
        result.setAllocation(allocation);
        repo.updateTaskState(responseId, "SETTLED");

        // 资金守恒核对：分配侧（SHARE+PLATFORM_CUT）应等于收入侧实收（SETTLE）
        boolean conserved = checkConservation(responseId, settleYuan, trace);

        // ⑪ 争议路径：计量补到 → 按更正口径重算 → 版本化更正账单（原始账单保留）
        if ("DISPUTED".equals(path)) {
            disputeCorrection(responseId, declaredKwh, metered, trace);
        }

        // 预占释放：任务正常完成（终态），按任务标识释放本次预占。
        // 取消/失败路径（本编排内不可达，生产由任务撤销入口调用 releaseByTask）；
        // 演示重置由 resetRuntimeState 整体清空。
        int released = reservationLedger.releaseByTask(responseId);
        trace.add("   预占释放：任务完成，按任务标识释放容量预占 " + released + " 条");

        result.setTrace(trace);
        log.info("闭环完成: {} 路径 {} 结算 {}元 守恒 {}", responseId, path, settleYuan, conserved);
        return result;
    }

    /**
     * 结算：合格率 + 偏差考核 + 结算电量 → 平台实收落账（账单 MERGE 幂等键）。
     */
    private BigDecimal settle(String responseId, BigDecimal declaredKwh, double actualKwh,
                              double contributionKwh, List<String> trace, DemoRunResult result) {
        double declared = declaredKwh.doubleValue();
        double passRate = metering.passRate(actualKwh, declared);
        double penalty = assessor.assess(passRate, declared, PRICE_YUAN_PER_KWH);
        double settleKwh = assessor.settleKwh(passRate, declared, actualKwh);
        BigDecimal settleYuan = BigDecimal.valueOf(settleKwh * PRICE_YUAN_PER_KWH)
                .setScale(2, RoundingMode.HALF_UP);
        result.setPassRatePct(BigDecimal.valueOf(passRate).setScale(1, RoundingMode.HALF_UP));
        result.setPenaltyYuan(BigDecimal.valueOf(penalty).setScale(2, RoundingMode.HALF_UP));
        result.setSettleYuan(settleYuan);
        repo.saveBill(responseId, "PLATFORM", settleYuan, "SETTLE",
                "平台实收结算金额[" + BILL_VERSION_ORIGINAL + "]");
        trace.add(String.format("⑨ 结算：合格率 %.1f%%，结算电量 %.0f kWh，考核 %.2f 元，平台实收 %.2f 元",
                passRate, settleKwh, penalty, settleYuan.doubleValue()));
        return settleYuan;
    }

    /**
     * 资金守恒核对：分配侧（SHARE+PLATFORM_CUT）应等于收入侧实收（SETTLE）。
     */
    private boolean checkConservation(String responseId, BigDecimal settleYuan, List<String> trace) {
        BigDecimal allocSum = repo.allocationSum(responseId);
        boolean conserved = allocSum.compareTo(settleYuan) == 0;
        trace.add(String.format("⑩ 分摊守恒核对：分配侧 %.2f 元 = 实收 %.2f 元 → %s",
                allocSum.doubleValue(), settleYuan.doubleValue(), conserved ? "守恒✓" : "不守恒✗"));
        if (!conserved) {
            throw new IllegalStateException("资金守恒核对失败: " + responseId
                    + " 分配侧 " + allocSum + " != 实收 " + settleYuan);
        }
        return true;
    }

    // ---------------- 内部步骤 ----------------

    private List<SamplePoint> buildSamples(LocalDate responseDay) {
        // 响应日前 5 个同类型日同时段负荷（教学数据，均值 1000）
        List<SamplePoint> samples = new ArrayList<>();
        double[] loads = {1000, 1000, 1000, 1000, 1000};
        for (int i = 0; i < loads.length; i++) {
            samples.add(new SamplePoint(responseDay.minusDays(i + 1), loads[i]));
        }
        return samples;
    }

    private List<AssessedResource> buildMembers(String path) {
        // 园区 3 类资源：储能 / 空调 / 充电桩（教学容量；置信度、可持续时长、合同有效）
        List<AssessedResource> members = new ArrayList<>();
        members.add(new AssessedResource("ess-001", "node-A", BigDecimal.valueOf(500),
                BigDecimal.valueOf(0.95), 3600, true));
        members.add(new AssessedResource("ac-001", "node-A", BigDecimal.valueOf(400),
                BigDecimal.valueOf(0.90), 3600, true));
        members.add(new AssessedResource("ev-001", "node-A", BigDecimal.valueOf(300),
                BigDecimal.valueOf(0.85), 3600, true));
        if ("DEGRADED".equals(path)) {
            // 降级路径：充电桩掉线（有效能力归零，从成员中剔除）
            members.removeIf(m -> "ev-001".equals(m.getResourceId()));
        }
        return members;
    }

    private void dispatchAndVerify(String responseId, Map<String, BigDecimal> plan, List<String> trace) {
        int i = 0;
        for (Map.Entry<String, BigDecimal> e : plan.entrySet()) {
            String instructionId = responseId + "-ins-" + (++i);
            DispatchInstruction instruction = new DispatchInstruction(
                    instructionId, e.getKey(), e.getValue(),
                    "activePower", e.getValue());
            instructionService.send(instruction);
            repo.saveInstruction(instructionId, responseId, e.getKey(), e.getValue(),
                    "SENT", System.currentTimeMillis(), null);
            trace.add("⑥ 指令下发：" + instructionId + " → " + e.getKey() + " " + e.getValue() + " kW");

            // 模拟设备回执与遥测达标（本地模拟设备，教学回环）
            instructionService.onAck(instructionId);
            instructionService.onActStarted(instructionId);
            instructionService.onTelemetry(instructionId, e.getValue()); // 进入容差带
            instructionService.onTelemetry(instructionId, e.getValue()); // 稳定窗口内持续达标
            boolean ok = instructionRepository.require(instructionId).getState().name().equals("COMPLETED");
            repo.updateInstructionState(instructionId, ok ? "COMPLETED" : "REVIEW",
                    ok ? System.currentTimeMillis() : null);
            trace.add("⑦ 执行核验：" + instructionId + " " + (ok ? "遥测连续稳定达标 COMPLETED" : "转核查 REVIEW"));
        }
    }

    private MeteringResult computeResponse(String responseId, List<SamplePoint> samples,
                                           LocalDate responseDay, List<String> trace) {
        double baseline = baselineCalculator.calculate(samples, responseDay);
        Double[] baselineArr = new Double[POINTS];
        Double[] actualArr = new Double[POINTS];
        for (int i = 0; i < POINTS; i++) {
            baselineArr[i] = baseline;
            actualArr[i] = ACTUAL_KW;
            repo.saveBaselinePoint(responseId, i, RULE_VERSION,
                    BigDecimal.valueOf(baseline).setScale(3, RoundingMode.HALF_UP),
                    BigDecimal.valueOf(ACTUAL_KW).setScale(3, RoundingMode.HALF_UP));
        }
        MeteringResult metered = metering.meterWithGapCheck(baselineArr, actualArr);
        trace.add(String.format("⑧ 响应量：基线 %.0f kW、实测 %.0f kW × %d 时段，正偏差积分 = %.0f kWh（缺失 %d 点）",
                baseline, ACTUAL_KW, POINTS, metered.responseKwh(), metered.missingPoints()));
        return metered;
    }

    /**
     * 分摊 + 账单：保底 50% 按申报容量占比，分成部分按实际贡献（教学演示：3 用户）。
     * 恢复幂等口径：分摊类账单（SHARE / PLATFORM_CUT）先删后写——全量替换，
     * 既可补齐"实收已写、分摊未写"的中断残留，也可修正半成品分摊，且金额不重复；
     * 实收（SETTLE）不受影响（MERGE 幂等键原地覆盖同值）。
     */
    private Map<String, BigDecimal> allocateAndBill(String responseId, BigDecimal settleYuan,
                                                    String billVersion, List<String> trace) {
        repo.deleteAllocationBills(responseId);

        BigDecimal guaranteed = settleYuan.multiply(BigDecimal.valueOf(0.5))
                .setScale(2, RoundingMode.HALF_UP);
        Map<String, BigDecimal> declaredKw = new LinkedHashMap<>();
        declaredKw.put("user-storage", BigDecimal.valueOf(500));
        declaredKw.put("user-ac", BigDecimal.valueOf(400));
        declaredKw.put("user-ev", BigDecimal.valueOf(300));
        Map<String, BigDecimal> actualKwh = new LinkedHashMap<>();
        actualKwh.put("user-storage", BigDecimal.valueOf(250));
        actualKwh.put("user-ac", BigDecimal.valueOf(200));
        actualKwh.put("user-ev", BigDecimal.valueOf(150));

        Map<String, BigDecimal> allocation = allocator.allocate(settleYuan, guaranteed, declaredKw, actualKwh);
        BigDecimal platformCut = settleYuan.subtract(guaranteed)
                .multiply(BigDecimal.valueOf(PLATFORM_CUT_RATE)).setScale(2, RoundingMode.HALF_UP);
        repo.saveBill(responseId, "PLATFORM", platformCut, "PLATFORM_CUT",
                "平台服务费10%[" + billVersion + "]");
        allocation.forEach((user, amt) ->
                repo.saveBill(responseId, user, amt, "SHARE", "保底+分成[" + billVersion + "]"));
        trace.add("   分摊明细：" + allocation + "，平台服务费 " + platformCut + " 元[" + billVersion + "]");
        return allocation;
    }

    /**
     * 争议更正：计量补到数据到达后按更正口径重算，生成版本化更正账单。
     * 教学模拟：第 2 时段（point_index=1）实测由 400 修正为 380 kW，
     * 更正响应量 615 kWh > 申报 600 → 仍按申报封顶结算，金额与原版一致；
     * 更正价值在于按争议口径重算并留痕：原始账单（V1）保留不删，
     * 更正账单（CORRECTION，V2）记录重算差额，可追溯、可冲正。
     */
    private void disputeCorrection(String responseId, BigDecimal declaredKwh,
                                   MeteringResult metered, List<String> trace) {
        // 按争议口径重算响应量（第 2 时段实测 400→380）；
        // 原始基线点与原始账单均保留不改，更正结果只以更正账单（V2）留痕
        double correctedKwh = 0;
        for (int i = 0; i < POINTS; i++) {
            double actual = (i == 1) ? DISPUTED_CORRECTED_KW : ACTUAL_KW;
            correctedKwh += (BASELINE_KW - actual) * 0.25;
        }
        double declared = declaredKwh.doubleValue();
        double passRate = metering.passRate(correctedKwh, declared);
        double settleKwh = assessor.settleKwh(passRate, declared, correctedKwh);
        BigDecimal correctedYuan = BigDecimal.valueOf(settleKwh * PRICE_YUAN_PER_KWH)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal originalYuan = repo.settleAmount(responseId);
        BigDecimal diffYuan = correctedYuan.subtract(originalYuan);

        repo.saveBill(responseId, "PLATFORM", diffYuan, "CORRECTION",
                "计量补到争议更正[" + BILL_VERSION_CORRECTED + "，基期" + BILL_VERSION_ORIGINAL
                        + " 实收 " + originalYuan + " 元，更正后 " + correctedYuan + " 元]");
        trace.add(String.format(
                "⑪ 争议更正：计量补到（第2时段实测 %.0f→%.0f kW），更正响应量 %.0f kWh，"
                        + "更正实收 %.2f 元，与基期差额 %.2f 元 → 生成更正账单[%s]，原始账单保留",
                ACTUAL_KW, DISPUTED_CORRECTED_KW, correctedKwh,
                correctedYuan.doubleValue(), diffYuan.doubleValue(), BILL_VERSION_CORRECTED));
        log.info("争议更正完成: {} 更正实收 {} 差额 {}", responseId, correctedYuan, diffYuan);
    }

    /**
     * 重置演示运行状态：库表数据已由控制器 deleteAll 清理，这里清空内存运行态——
     * 容量预占台账 + 内存指令仓库。修复历史缺陷：重置只清库不清内存，旧预占残留
     * 挤占剩余能力，重置后再运行正常案例被误报缺口（700+ kW）。
     */
    public void resetRuntimeState() {
        reservationLedger.clear();
        instructionRepository.clear();
        log.info("演示运行状态已重置：容量预占台账 + 内存指令仓库已清空");
    }

    /**
     * 取消任务的预占释放入口（生产任务撤销/失败时调用）：
     * 按任务标识释放容量预占，返回释放条数。
     */
    public int releaseReservation(String responseId) {
        int released = reservationLedger.releaseByTask(responseId);
        log.info("按任务释放容量预占: {} 释放 {} 条", responseId, released);
        return released;
    }
}
