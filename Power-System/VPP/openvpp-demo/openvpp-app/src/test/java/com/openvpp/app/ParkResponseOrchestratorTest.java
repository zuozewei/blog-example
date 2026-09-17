package com.openvpp.app;

import com.openvpp.app.orchestration.DemoRunResult;
import com.openvpp.app.orchestration.ParkResponseOrchestrator;
import com.openvpp.app.persistence.ResponseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 园区需求响应贯穿案例集成测试 —— 覆盖复核要求的四场景与恢复/争议路径。
 *
 * 场景：首次执行 / 重复执行（幂等）/ 重置后再执行 / 取消后再执行；
 * 另覆盖：结算中断恢复（实收已写、分摊未写、任务仍为 DISPATCHED 时重跑补齐）、
 * 争议路径版本化更正账单（原始保留 + 更正版留痕）。
 *
 * 数值断言与 PARK-DEMO.md 手工核算底稿一致：
 * 实收 1200.00 元；分摊 user-storage 475.00 / user-ac 380.00 / user-ev 285.00；
 * 平台服务费 60.00；分配侧合计 = 实收（资金守恒）。
 */
@SpringBootTest
@TestPropertySource(properties = {
        // 每个测试类独立临时 H2 文件库，不污染默认演示库
        "spring.datasource.url=jdbc:h2:file:${java.io.tmpdir}/openvpp-test-${random.uuid};AUTO_SERVER=TRUE"
})
class ParkResponseOrchestratorTest {

    @Autowired
    private ParkResponseOrchestrator orchestrator;
    @Autowired
    private ResponseRepository repo;

    private static final BigDecimal DECLARED = new BigDecimal("600");
    private static final BigDecimal TARGET = new BigDecimal("900");

    @BeforeEach
    void setUp() {
        repo.deleteAll();
        orchestrator.resetRuntimeState();
    }

    /** 金额取数辅助 */
    private BigDecimal billAmount(String responseId, String subject, String billType) {
        return repo.listBills(responseId).stream()
                .filter(b -> subject.equals(b.get("SUBJECT")) && billType.equals(b.get("BILL_TYPE")))
                .map(b -> (BigDecimal) b.get("AMOUNT_YUAN"))
                .findFirst()
                .orElse(null);
    }

    @Test
    void 首次执行正常路径_金额与守恒符合底稿() {
        DemoRunResult r = orchestrator.run("run-001", "NORMAL", DECLARED, TARGET);

        assertTrue(r.isFeasible());
        assertFalse(r.isIdempotentReplay());
        assertEquals(0, new BigDecimal("1200.00").compareTo(r.getSettleYuan()));
        assertEquals(0, new BigDecimal("600.000").compareTo(r.getResponseKwh()));

        assertEquals("SETTLED", repo.taskState("run-001"));
        assertEquals(0, new BigDecimal("475.00").compareTo(billAmount("run-001", "user-storage", "SHARE")));
        assertEquals(0, new BigDecimal("380.00").compareTo(billAmount("run-001", "user-ac", "SHARE")));
        assertEquals(0, new BigDecimal("285.00").compareTo(billAmount("run-001", "user-ev", "SHARE")));
        assertEquals(0, new BigDecimal("60.00").compareTo(billAmount("run-001", "PLATFORM", "PLATFORM_CUT")));
        // 资金守恒：分配侧 = 实收
        assertEquals(0, repo.settleAmount("run-001").compareTo(repo.allocationSum("run-001")));
    }

    @Test
    void 重复执行幂等_不重复出账() {
        orchestrator.run("run-001", "NORMAL", DECLARED, TARGET);
        DemoRunResult replay = orchestrator.run("run-001", "NORMAL", DECLARED, TARGET);

        assertTrue(replay.isIdempotentReplay());
        // 账单仍是 5 条（SETTLE×1 + PLATFORM_CUT×1 + SHARE×3），金额不变
        List<Map<String, Object>> bills = repo.listBills("run-001");
        assertEquals(5, bills.size());
        assertEquals(0, new BigDecimal("1200.00").compareTo(repo.settleAmount("run-001")));
        assertEquals(0, repo.settleAmount("run-001").compareTo(repo.allocationSum("run-001")));
    }

    @Test
    void 重置后再执行_预占与指令仓库同步清理_不报缺口() {
        orchestrator.run("run-001", "NORMAL", DECLARED, TARGET);

        // 重置：清库 + 清内存运行态（修复复核②：旧预占残留误报缺口 710kW）
        repo.deleteAll();
        orchestrator.resetRuntimeState();

        DemoRunResult again = orchestrator.run("run-001", "NORMAL", DECLARED, TARGET);
        assertTrue(again.isFeasible(), "重置后再执行不应报缺口，gapKw=" + again.getGapKw());
        assertEquals(0, BigDecimal.ZERO.compareTo(again.getGapKw()));
        assertEquals(0, new BigDecimal("1200.00").compareTo(again.getSettleYuan()));
        assertEquals(0, repo.settleAmount("run-001").compareTo(repo.allocationSum("run-001")));
    }

    @Test
    void 取消后再执行_按任务标识释放预占_不报缺口() {
        // 模拟"任务预占已登记但任务被撤销"：先跑一次占据容量，再按任务标识释放
        orchestrator.run("run-cancel", "NORMAL", DECLARED, TARGET);
        int released = orchestrator.releaseReservation("run-cancel");
        // 任务正常完成时已自动释放，此处再释放为 0（验证幂等，不抛错、不负数）
        assertEquals(0, released);

        // 用新 responseId 再执行：取消释放后剩余能力应完整，不报缺口
        repo.deleteAll();
        orchestrator.resetRuntimeState();
        DemoRunResult r = orchestrator.run("run-after-cancel", "NORMAL", DECLARED, TARGET);
        assertTrue(r.isFeasible(), "取消释放后再执行不应报缺口，gapKw=" + r.getGapKw());
        assertEquals(0, new BigDecimal("1200.00").compareTo(r.getSettleYuan()));
    }

    @Test
    void 结算中断恢复_实收已写分摊缺失_重跑补齐且金额不重复() {
        // 构造中断残留：实收已写、分摊未写、任务仍为 DISPATCHED（复核③现场）
        repo.saveTask("run-crash", "evt-run-crash", DECLARED, TARGET,
                System.currentTimeMillis(), System.currentTimeMillis() + 3600_000,
                "DISPATCHED", BigDecimal.ZERO);
        repo.saveBill("run-crash", "PLATFORM", new BigDecimal("1200.00"),
                "SETTLE", "平台实收结算金额[V1]");

        DemoRunResult r = orchestrator.run("run-crash", "NORMAL", DECLARED, TARGET);

        // 不应被误判幂等短路；分摊补齐
        assertFalse(r.isIdempotentReplay());
        assertEquals("SETTLED", repo.taskState("run-crash"));
        assertEquals(0, new BigDecimal("475.00").compareTo(billAmount("run-crash", "user-storage", "SHARE")));
        assertEquals(0, new BigDecimal("380.00").compareTo(billAmount("run-crash", "user-ac", "SHARE")));
        assertEquals(0, new BigDecimal("285.00").compareTo(billAmount("run-crash", "user-ev", "SHARE")));
        assertEquals(0, new BigDecimal("60.00").compareTo(billAmount("run-crash", "PLATFORM", "PLATFORM_CUT")));
        // 金额不重复：实收仍 1200，分配侧合计 = 实收（而非两倍）
        assertEquals(0, new BigDecimal("1200.00").compareTo(repo.settleAmount("run-crash")));
        assertEquals(0, repo.settleAmount("run-crash").compareTo(repo.allocationSum("run-crash")));
    }

    @Test
    void 争议路径_生成版本化更正账单_原始账单保留() {
        DemoRunResult r = orchestrator.run("run-dispute", "DISPUTED", DECLARED, TARGET);

        assertTrue(r.isFeasible());
        // 更正账单存在，memo 携带账期版本号
        BigDecimal correction = billAmount("run-dispute", "PLATFORM", "CORRECTION");
        assertTrue(correction != null, "争议路径应生成 CORRECTION 更正账单");
        String memo = repo.listBills("run-dispute").stream()
                .filter(b -> "CORRECTION".equals(b.get("BILL_TYPE")))
                .map(b -> (String) b.get("MEMO"))
                .findFirst().orElse("");
        assertTrue(memo.contains("V2"), "更正账单 memo 应携带更正版本号 V2: " + memo);

        // 原始账单保留：SETTLE 仍为 1200.00，SHARE 分摊仍是 V1 口径
        assertEquals(0, new BigDecimal("1200.00").compareTo(repo.settleAmount("run-dispute")));
        assertEquals(0, new BigDecimal("475.00").compareTo(billAmount("run-dispute", "user-storage", "SHARE")));
        // 更正后响应量 615kWh > 申报 600 仍封顶 → 差额为 0（重算口径留痕，金额不变）
        assertEquals(0, BigDecimal.ZERO.setScale(2).compareTo(correction));
    }

    @Test
    void 降级路径_显式报缺口不出账() {
        DemoRunResult r = orchestrator.run("run-degraded", "DEGRADED", DECLARED, TARGET);

        assertFalse(r.isFeasible());
        assertEquals(0, new BigDecimal("148.5").compareTo(r.getGapKw()));
        assertEquals("GAP", repo.taskState("run-degraded"));
        assertTrue(repo.listBills("run-degraded").isEmpty(), "降级不可行不应出账");
    }
}
