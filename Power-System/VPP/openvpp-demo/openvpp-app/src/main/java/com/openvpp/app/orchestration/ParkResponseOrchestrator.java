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
 *   响应电量 600×1h = 600 kWh；补偿单价 2 元/kWh；总补偿 1200 元；
 *   平台服务费 10%（120 元）；用户可分配 1080 元。
 *
 * 幂等：同一 responseId 重复执行不重复下发、不重复出账（指令级幂等 +
 *       账单 MERGE 幂等键 + 任务已结算短路）。重启后任务与账单仍在（H2 文件库）。
 */
@Service
public class ParkResponseOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ParkResponseOrchestrator.class);

    // ---- 教学假设常量（与文章手工核算底稿一一对应） ----
    public static final double BASELINE_KW = 1000.0;
    public static final double ACTUAL_KW = 400.0;
    public static final int POINTS = 4;                       // 4 个 15 分钟点
    public static final double PRICE_YUAN_PER_KWH = 2.0;      // 假设补偿单价
    public static final double PLATFORM_CUT_RATE = 0.10;      // 平台服务费 10%
    public static final String RULE_VERSION = "TEACH-2026.1";

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
    public DemoRunResult run(String responseId, String path,
                             BigDecimal declaredKwh, BigDecimal targetKw) {
        DemoRunResult result = new DemoRunResult();
        result.setResponseId(responseId);
        result.setPath(path);
        List<String> trace = new ArrayList<>();

        boolean alreadySettled = repo.taskExists(responseId)
                && repo.billExists(responseId, "PLATFORM", "SETTLE");
        result.setIdempotentReplay(alreadySettled);
        if (alreadySettled) {
            trace.add("幂等拦截：responseId=" + responseId + " 已结算，不重复执行/出账，直接返回既有结果");
            log.warn("幂等拦截: {} 已结算，跳过重复执行", responseId);
            result.setTrace(trace);
            return result;
        }

        // ① 模拟遥测 → 接入校验 → 数据入库（基线样本与实测点）
        trace.add("① 遥测接入：采集基线样本与响应期实测，校验通过后入库");
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
        InstructionDecomposer.DecompositionResult decomp = decomposer.decompose(
                members, committablePool, targetKw, AssessedResource.Direction.DOWN,
                window, reservationLedger);
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
        trace.add("⑤ 指令分解：可行，按成员有效能力等比分配，登记容量预占");

        // ⑥ 指令下发 → ⑦ 执行核验（遥测驱动）
        dispatchAndVerify(responseId, decomp.getPlan(), trace);

        // ⑧ 响应量计算（基线 - 实测，正偏差积分，缺失/零值口径）
        MeteringResult metered = computeResponse(responseId, samples, responseDay, trace);
        result.setBaselineKw(BigDecimal.valueOf(BASELINE_KW));
        result.setActualKw(BigDecimal.valueOf(ACTUAL_KW));
        result.setResponseKwh(BigDecimal.valueOf(metered.responseKwh()).setScale(3, RoundingMode.HALF_UP));

        // ⑨ 结算（合格率 + 偏差考核 + 结算电量）
        double declared = declaredKwh.doubleValue();
        double actual = metered.responseKwh();
        double passRate = metering.passRate(actual, declared);
        double penalty = assessor.assess(passRate, declared, PRICE_YUAN_PER_KWH);
        double settleKwh = assessor.settleKwh(passRate, declared, actual);
        BigDecimal settleYuan = BigDecimal.valueOf(settleKwh * PRICE_YUAN_PER_KWH)
                .setScale(2, RoundingMode.HALF_UP);
        result.setPassRatePct(BigDecimal.valueOf(passRate).setScale(1, RoundingMode.HALF_UP));
        result.setPenaltyYuan(BigDecimal.valueOf(penalty).setScale(2, RoundingMode.HALF_UP));
        result.setSettleYuan(settleYuan);
        trace.add(String.format("⑨ 结算：合格率 %.1f%%，结算电量 %.0f kWh，考核 %.2f 元，平台实收 %.2f 元",
                passRate, settleKwh, penalty, settleYuan.doubleValue()));

        // ⑩ 分摊 + 账单（幂等 MERGE）
        Map<String, BigDecimal> allocation = allocateAndBill(responseId, settleYuan, trace);
        result.setAllocation(allocation);
        repo.updateTaskState(responseId, "SETTLED");

        // 资金守恒核对：分配侧（SHARE+PLATFORM_CUT）应等于收入侧实收（SETTLE）
        BigDecimal allocSum = repo.allocationSum(responseId);
        boolean conserved = allocSum.compareTo(settleYuan) == 0;
        trace.add(String.format("⑩ 分摊守恒核对：分配侧 %.2f 元 = 实收 %.2f 元 → %s",
                allocSum.doubleValue(), settleYuan.doubleValue(), conserved ? "守恒✓" : "不守恒✗"));
        result.setTrace(trace);
        log.info("闭环完成: {} 路径 {} 响应量 {}kWh 结算 {}元", responseId, path, actual, settleYuan);
        return result;
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

    private Map<String, BigDecimal> allocateAndBill(String responseId, BigDecimal settleYuan,
                                                    List<String> trace) {
        // 平台实收
        repo.saveBill(responseId, "PLATFORM", settleYuan, "SETTLE", "平台实收结算金额");

        // 保底 50% 按申报容量占比，分成部分按实际贡献（教学演示：3 用户）
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
        repo.saveBill(responseId, "PLATFORM", platformCut, "PLATFORM_CUT", "平台服务费10%");
        allocation.forEach((user, amt) ->
                repo.saveBill(responseId, user, amt, "SHARE", "保底+分成"));
        trace.add("   分摊明细：" + allocation + "，平台服务费 " + platformCut + " 元");
        return allocation;
    }
}
