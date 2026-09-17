package com.openvpp.aggregator;

import com.openvpp.aggregator.engine.AssessedResource;
import com.openvpp.aggregator.engine.CapacityPoolCalculator;
import com.openvpp.aggregator.engine.CapacityReservationLedger;
import com.openvpp.aggregator.engine.InstructionDecomposer;
import com.openvpp.aggregator.engine.TaskWindow;
import com.openvpp.aggregator.engine.UnitGrouper;
import com.openvpp.aggregator.unit.VppUnit;
import com.openvpp.common.enums.Scenario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 聚合引擎全场景单测：同节点分组 / 准入门槛 / 容量池口径 / 指令分解正确性。
 * 分解验收 4 条断言并列：① 每资源 0 ≤ 分配 ≤ 有效能力；② 不突破能力边界（含预占扣除）；
 * ③ 重叠任务无重复占用；④ 不可行时返回缺口而非硬凑。
 */
class AggregatorEngineTest {

    private final UnitGrouper grouper = new UnitGrouper();
    private final CapacityPoolCalculator poolCalc = new CapacityPoolCalculator(0.9);
    private final InstructionDecomposer decomposer = new InstructionDecomposer();

    private static AssessedResource res(String id, String node, double kw, double conf, boolean valid) {
        return new AssessedResource(id, node,
                BigDecimal.valueOf(kw), BigDecimal.valueOf(conf), 3600, valid);
    }

    @Test
    void 同节点聚成单元跨节点分单元() {
        List<AssessedResource> pool = List.of(
                res("es-001", "N1", 1000, 0.95, true),
                res("ac-001", "N1", 500, 0.9, true),
                res("pv-001", "N2", 800, 0.7, true));

        List<VppUnit> units = grouper.group(pool, Scenario.PEAK_SHIFT);

        assertEquals(2, units.size(), "跨节点资源必须分成不同单元");
        VppUnit n1 = units.stream().filter(u -> "N1".equals(u.getClearingNodeId())).findFirst().orElseThrow();
        assertEquals(2, n1.getResourceIds().size());
    }

    @Test
    void 代理失效资源不入单元() {
        List<AssessedResource> pool = List.of(
                res("es-001", "N1", 1000, 0.95, true),
                res("es-expired", "N1", 2000, 0.95, false));   // 代理已到期

        List<VppUnit> units = grouper.group(pool, Scenario.PEAK_SHIFT);
        assertEquals(1, units.get(0).getResourceIds().size(),
                "代理失效资源必须被排除");
    }

    @Test
    void 容量池按置信度折扣求和再打聚合折扣() {
        List<AssessedResource> members = List.of(
                res("es-001", "N1", 1000, 0.95, true),    // 可信 950
                res("ac-001", "N1", 1200, 0.9, true),     // 可信 1080
                res("pv-001", "N1", 800, 0.7, true));     // 可信 560

        BigDecimal pool = poolCalc.poolOf(members);
        // (950+1080+560) × 0.9 = 2590 × 0.9 = 2331
        assertEquals(0, pool.compareTo(new BigDecimal("2331.000")),
                "容量池口径错误: " + pool);
        assertTrue(pool.compareTo(
                BigDecimal.valueOf(1000 + 1200 + 800)) < 0,
                "承诺容量必须小于铭牌加总");
    }

    @Test
    void 指令分解按比例且总和守恒且不破能力上界() {
        List<AssessedResource> members = List.of(
                res("es-001", "N1", 1000, 0.95, true),    // 可信 950
                res("ac-001", "N1", 1200, 0.9, true),     // 可信 1080
                res("pv-001", "N1", 800, 0.7, true));     // 可信 560
        BigDecimal pool = poolCalc.poolOf(members);       // 可承诺 2331
        BigDecimal command = new BigDecimal("1500");

        InstructionDecomposer.DecompositionResult result =
                decomposer.decompose(members, pool, command);

        assertTrue(result.isFeasible(), "1500 ≤ 2331 必须可行");
        Map<String, BigDecimal> plan = result.getPlan();
        BigDecimal sum = plan.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, sum.compareTo(command), "分解总和必须守恒: " + sum);
        // 断言①②：每个资源 0 ≤ 分配 ≤ 自身有效能力
        for (AssessedResource m : members) {
            BigDecimal share = plan.get(m.getResourceId());
            assertNotNull(share);
            assertTrue(share.signum() >= 0, "分配不得为负: " + m.getResourceId());
            assertTrue(share.compareTo(m.credibleCapacityKw()) <= 0,
                    "分配不得突破有效能力上界: " + m.getResourceId());
        }
        // 储能可信容量最大，应分得最大份额
        assertTrue(plan.get("es-001").compareTo(plan.get("pv-001")) > 0);
    }

    @Test
    void 指令超可承诺容量返回缺口而非硬凑() {
        List<AssessedResource> members = List.of(res("es-001", "N1", 1000, 0.95, true));
        BigDecimal pool = poolCalc.poolOf(members);       // 950 × 0.9 = 855
        BigDecimal command = new BigDecimal("9999");

        InstructionDecomposer.DecompositionResult result =
                decomposer.decompose(members, pool, command);

        // 断言④：不可行时必须明确返回缺口量，不得静默截断
        assertFalse(result.isFeasible());
        assertEquals(0, result.getGapKw().compareTo(new BigDecimal("9144")),
                "缺口量 = 指令 - 可承诺容量: " + result.getGapKw());
        assertTrue(result.getPlan().isEmpty(), "不可行时不得给出分配方案");
    }

    @Test
    void 审稿人缺陷案例12乘100折扣09任务1080不得负分配() {
        // 缺陷案例：12 个资源各 100 kW 可信容量，聚合折扣 0.9，可承诺 1080 kW。
        // 旧算法用折扣后的 1080 作分母、折扣前的 100 作权重，
        // 初步分配总量虚高至 1200 kW，差额 −120 kW 压给末位成员得 −20 kW。
        List<AssessedResource> members = java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(i -> res("res-" + i, "N1", 100, 1.0, true))
                .collect(java.util.stream.Collectors.toList());
        BigDecimal committable = poolCalc.poolOf(members);   // 1200 × 0.9 = 1080
        assertEquals(0, committable.compareTo(new BigDecimal("1080.0")));

        InstructionDecomposer.DecompositionResult result =
                decomposer.decompose(members, committable, new BigDecimal("1080"));

        // 断言④：可承诺容量口径下任务恰可行
        assertTrue(result.isFeasible(), "任务等于可承诺容量必须可行");
        Map<String, BigDecimal> plan = result.getPlan();
        // 断言①：任何成员不得出现负分配
        plan.forEach((id, share) -> assertTrue(share.signum() >= 0,
                "负分配复现: " + id + " = " + share));
        // 断言②：不突破能力边界（每个成员可信容量 100 kW）
        plan.forEach((id, share) -> assertTrue(
                share.compareTo(new BigDecimal("100.0")) <= 0,
                "分配突破能力上界: " + id + " = " + share));
        // 守恒：等比分配 12 × 90 = 1080，总和严格相等
        BigDecimal sum = plan.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, sum.compareTo(new BigDecimal("1080")), "总和必须守恒: " + sum);
        plan.forEach((id, share) -> assertEquals(0, share.compareTo(new BigDecimal("90")),
                "等权重成员应等额分配: " + id + " = " + share));
    }

    @Test
    void 舍入残差只分配给仍有可用余量的成员() {
        // 3 个成员各 100 kW，聚合折扣取 1.0（教学口径：不打折场景），
        // 指令 299.999 kW 贴近有效能力总和 300 kW。
        // 初步分配各 floor3(299.999×100/300) = 99.999，残差 0.002；
        // 每个成员余量仅 0.001，残差必须分拆给有余量者，任何成员不得突破 100 kW 上界。
        List<AssessedResource> members = List.of(
                new AssessedResource("res-a", "N1", new BigDecimal("100"), BigDecimal.ONE, 3600, true),
                new AssessedResource("res-b", "N1", new BigDecimal("100"), BigDecimal.ONE, 3600, true),
                new AssessedResource("res-c", "N1", new BigDecimal("100"), BigDecimal.ONE, 3600, true));
        BigDecimal committable = new CapacityPoolCalculator(1.0).poolOf(members);   // 300

        InstructionDecomposer.DecompositionResult result =
                decomposer.decompose(members, committable, new BigDecimal("299.999"));

        assertTrue(result.isFeasible());
        Map<String, BigDecimal> plan = result.getPlan();
        // 守恒：残差归并后总和严格相等
        BigDecimal sum = plan.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, sum.compareTo(new BigDecimal("299.999")), "总和必须守恒: " + sum);
        // 断言②：残差按余量分拆，res-a/res-b 恰好顶到上界 100，res-c 保持 99.999；
        // 旧"残差全压一个成员"逻辑会分出 100.001 kW，突破能力上界
        assertEquals(0, plan.get("res-a").compareTo(new BigDecimal("100.000")),
                "残差不得把成员顶破有效能力上界: " + plan.get("res-a"));
        assertEquals(0, plan.get("res-b").compareTo(new BigDecimal("100.000")),
                "残差不得把成员顶破有效能力上界: " + plan.get("res-b"));
        assertEquals(0, plan.get("res-c").compareTo(new BigDecimal("99.999")),
                "余量耗尽的成员不得再承接残差: " + plan.get("res-c"));
    }

    @Test
    void 重叠时间窗任务不得重复占用同一资源容量() {
        List<AssessedResource> members = List.of(res("es-001", "N1", 1000, 1.0, true));
        CapacityReservationLedger ledger = new CapacityReservationLedger();
        TaskWindow window = new TaskWindow(1000L, 1800L);
        // 教学场景可承诺容量按任务取 800（等效运营批下来的执行口径）
        BigDecimal committable = new BigDecimal("800");

        InstructionDecomposer.DecompositionResult first =
                decomposer.decompose(members, committable, new BigDecimal("800"),
                        AssessedResource.Direction.DOWN, window, "task-1", ledger);
        assertTrue(first.isFeasible(), "首任务 800 kW 必须可行");

        // 断言③：同一资源同一重叠窗口再下发 200 kW，
        // 有效能力 1000 - 已预占 800 = 剩余 200，恰可行且二次分配 ≤ 剩余能力
        InstructionDecomposer.DecompositionResult second =
                decomposer.decompose(members, committable, new BigDecimal("200"),
                        AssessedResource.Direction.DOWN, window, "task-2", ledger);
        assertTrue(second.isFeasible());
        assertEquals(0, second.getPlan().get("es-001").compareTo(new BigDecimal("200")));

        // 第三次 1 kW：剩余能力为 0，断言②+④ —— 不得占用已预占容量，返回缺口
        InstructionDecomposer.DecompositionResult third =
                decomposer.decompose(members, committable, BigDecimal.ONE,
                        AssessedResource.Direction.DOWN, window, "task-3", ledger);
        assertFalse(third.isFeasible(), "重叠窗口内剩余能力为 0 时必须返回缺口");
        assertEquals(0, third.getGapKw().compareTo(BigDecimal.ONE));

        // 台账口径校验：重叠窗口累计占用 = 800 + 200 = 1000
        assertEquals(0, ledger.occupiedIn("es-001", window)
                .compareTo(new BigDecimal("1000")), "预占台账累计口径错误");
    }

    @Test
    void 资源掉线重估后无法完成的任务明确返回缺口() {
        // 4 个成员各 300 kW 可信容量，可承诺 1200 × 0.9 = 1080；下发 1000 kW 任务。
        List<AssessedResource> members = List.of(
                res("res-1", "N1", 300, 1.0, true),
                res("res-2", "N1", 300, 1.0, true),
                res("res-3", "N1", 300, 1.0, true),
                res("res-4", "N1", 300, 1.0, true));
        BigDecimal committable = poolCalc.poolOf(members);   // 1080
        BigDecimal command = new BigDecimal("1000");
        CapacityReservationLedger ledger = new CapacityReservationLedger();
        TaskWindow window = new TaskWindow(2000L, 1800L);

        InstructionDecomposer.DecompositionResult before =
                decomposeWith(members, committable, command, window, ledger);
        assertTrue(before.isFeasible(), "掉线前 1000 ≤ 1080 必须可行");
        // 教学简化：分解结果尚未下发落账即发生掉线，释放全部预占后重估
        // （生产实现应为"已下发指令"与"预占"两个台账，已下发的部分随资源失联由兜底资源承接）
        members.forEach(m -> ledger.releaseAll(m.getResourceId()));

        // 掉线重估：剩余 3 个成员有效能力之和 900 < 指令 1000，
        // 断言④ —— 必须返回缺口 100，不得静默截断成 900 硬凑
        List<AssessedResource> survivors = members.subList(0, 3);
        InstructionDecomposer.DecompositionResult after =
                decomposeWith(survivors, committable, command, window, ledger);
        assertFalse(after.isFeasible(), "掉线后能力不足必须判不可行");
        assertEquals(0, after.getGapKw().compareTo(new BigDecimal("100")),
                "缺口量 = 指令 - 剩余有效能力之和: " + after.getGapKw());
        assertTrue(after.getPlan().isEmpty());
    }

    /** 便利方法：显式台账 + 窗口，方向取下调 */
    private InstructionDecomposer.DecompositionResult decomposeWith(
            List<AssessedResource> members, BigDecimal committable, BigDecimal command,
            TaskWindow window, CapacityReservationLedger ledger) {
        return decomposer.decompose(members, committable, command,
                AssessedResource.Direction.DOWN, window, "task-decomposeWith", ledger);
    }
}
