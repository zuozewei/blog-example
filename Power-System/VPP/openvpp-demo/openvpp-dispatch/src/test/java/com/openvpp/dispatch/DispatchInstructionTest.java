package com.openvpp.dispatch;

import com.openvpp.dispatch.compensation.TimeoutCompensationTask;
import com.openvpp.dispatch.instruction.DispatchInstruction;
import com.openvpp.dispatch.instruction.InMemoryInstructionRepository;
import com.openvpp.dispatch.instruction.InstructionRepository;
import com.openvpp.dispatch.instruction.InstructionService;
import com.openvpp.dispatch.instruction.InstructionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 指令链路全场景单测。
 *
 * 覆盖审稿人验收案例：
 * ① 回执丢失但设备已执行（遥测达标判成功，不判失败重发）
 * ② 回执成功但功率无变化（ACK ≠ 响应，执行超时转核查）
 * ③ 执行中失联（转核查，不自动重发）
 * ④ 重复投递（回执/下发幂等，终态后迟到消息不激活）
 * ⑤ 平台重启（在途指令恢复超时检查）
 * 另保留：完整闭环 / 非法迁移拦截 / 响应时延口径 / 人工接管取消。
 */
class DispatchInstructionTest {

    private static final BigDecimal TOLERANCE_KW = new BigDecimal("10");
    private static final long STABLE_WINDOW_MS = 30;
    private static final long ACK_TIMEOUT_MS = 50;
    private static final long EXEC_TIMEOUT_MS = 50;

    private InstructionRepository repository;
    private InstructionService service;
    private TimeoutCompensationTask compensationTask;
    private List<DispatchInstruction> downlinkLog;

    @BeforeEach
    void setUp() {
        repository = new InMemoryInstructionRepository();
        downlinkLog = new ArrayList<>();
        service = new InstructionService(repository, downlinkLog::add,
                TOLERANCE_KW, STABLE_WINDOW_MS);
        compensationTask = new TimeoutCompensationTask(repository, service,
                ACK_TIMEOUT_MS, EXEC_TIMEOUT_MS);
    }

    /** 目标 -500kW，容差 10kW：容差带 [-510, -490] */
    private DispatchInstruction newInstruction(String id) {
        return new DispatchInstruction(id, "es-001",
                new BigDecimal("500"), "targetPower", new BigDecimal("-500"));
    }

    /** 连续两次带内遥测（间隔超过稳定窗口）驱动 COMPLETED */
    private void telemetryStable(String id) throws InterruptedException {
        service.onTelemetry(id, new BigDecimal("-505"));
        Thread.sleep(STABLE_WINDOW_MS + 20);
        service.onTelemetry(id, new BigDecimal("-505"));
    }

    @Test
    void 完整闭环走五个时间点() throws InterruptedException {
        DispatchInstruction instruction = newInstruction("ins-001");
        service.send(instruction);
        assertEquals(InstructionState.SENT, instruction.getState());
        assertEquals(1, downlinkLog.size(), "下发必须投递下行通道");
        assertNotNull(instruction.getSentAt());

        service.onAck("ins-001");
        assertEquals(InstructionState.ACKED, instruction.getState());
        assertNotNull(instruction.getAckedAt());

        service.onActStarted("ins-001");
        assertEquals(InstructionState.ACTING, instruction.getState());
        assertNotNull(instruction.getActStartedAt());

        telemetryStable("ins-001");
        assertEquals(InstructionState.COMPLETED, instruction.getState());
        assertNotNull(instruction.getReachedAt());
        assertNotNull(instruction.getStableAt());
    }

    @Test
    void 非法迁移一律拦截() throws InterruptedException {
        DispatchInstruction instruction = newInstruction("ins-002");
        // CREATED 不能直接 COMPLETED（必须经遥测达标驱动）
        assertThrows(IllegalStateException.class,
                () -> instruction.transitTo(InstructionState.COMPLETED));
        // COMPLETED 后不能再迁移
        service.send(instruction);
        telemetryStable("ins-002");
        assertEquals(InstructionState.COMPLETED, instruction.getState());
        assertThrows(IllegalStateException.class,
                () -> instruction.transitTo(InstructionState.SENT));
    }

    @Test
    void 响应时延以遥测达标为准而非ACK() {
        DispatchInstruction instruction = newInstruction("ins-003");
        service.send(instruction);
        assertNull(instruction.responseTimeMs(), "未达标时响应时延应为空");

        service.onAck("ins-003");
        assertNull(instruction.responseTimeMs(), "ACK 不代表响应，响应时延仍为空");
        assertNotNull(instruction.ackElapsedMs(), "ACK 只产出通信耗时指标");

        service.onTelemetry("ins-003", new BigDecimal("-505"));
        assertNotNull(instruction.responseTimeMs(), "遥测进入容差带才产生响应时延");
        assertTrue(instruction.responseTimeMs() >= 0);
    }

    @Test
    void 验收1_回执丢失但设备已执行_遥测达标判成功不重发() throws InterruptedException {
        DispatchInstruction instruction = newInstruction("ins-101");
        service.send(instruction);   // ACK 永远丢失

        telemetryStable("ins-101");   // 遥测证明设备已执行
        assertEquals(InstructionState.COMPLETED, instruction.getState(),
                "遥测连续稳定达标即可判成功，不依赖 ACK");
        assertEquals(1, downlinkLog.size(), "不得因 ACK 丢失而重发");
        assertEquals(0, compensationTask.runOnce(), "已完成指令不得再被补偿");
    }

    @Test
    void 验收2_回执成功但功率无变化_执行超时转核查() throws InterruptedException {
        DispatchInstruction instruction = newInstruction("ins-102");
        service.send(instruction);
        service.onAck("ins-102");

        service.onTelemetry("ins-102", new BigDecimal("0"));   // 功率纹丝不动
        assertEquals(InstructionState.ACKED, instruction.getState(),
                "ACK 不等于响应，未达标不得转 COMPLETED");

        Thread.sleep(EXEC_TIMEOUT_MS + 30);
        int reviewed = compensationTask.runOnce();
        assertEquals(1, reviewed);
        assertEquals(InstructionState.REVIEW, instruction.getState(),
                "执行超时且结果未知应转核查，不得判失败");
        assertEquals(1, downlinkLog.size(), "核查期间禁止自动重发");

        telemetryStable("ins-102");   // 迟到遥测证明设备其实执行了
        assertEquals(InstructionState.COMPLETED, instruction.getState(),
                "核查中遥测稳定达标可终结核查");
    }

    @Test
    void 验收3_执行中失联_转核查不重发() throws InterruptedException {
        DispatchInstruction instruction = newInstruction("ins-103");
        service.send(instruction);
        service.onAck("ins-103");
        service.onActStarted("ins-103");
        assertEquals(InstructionState.ACTING, instruction.getState());

        // 此后设备失联：无遥测、无回执
        Thread.sleep(EXEC_TIMEOUT_MS + 30);
        assertEquals(1, compensationTask.runOnce());
        assertEquals(InstructionState.REVIEW, instruction.getState());
        assertEquals(1, downlinkLog.size(), "失联只转核查，不得重发");
    }

    @Test
    void 验收4_重复投递幂等_终态后迟到消息不激活() throws InterruptedException {
        DispatchInstruction instruction = newInstruction("ins-104");
        service.send(instruction);
        service.send(instruction);   // 重复下发
        assertEquals(1, downlinkLog.size(), "重复下发被幂等拦截");

        service.onAck("ins-104");
        service.onAck("ins-104");     // 重复回执
        assertEquals(InstructionState.ACKED, instruction.getState(), "重复 ACK 幂等忽略");

        service.onActStarted("ins-104");
        service.onActStarted("ins-104");   // 重复动作上报
        assertEquals(InstructionState.ACTING, instruction.getState());

        telemetryStable("ins-104");
        assertEquals(InstructionState.COMPLETED, instruction.getState());

        // 终态后迟到消息一律不得重新激活执行
        service.onAck("ins-104");
        service.onActStarted("ins-104");
        service.onTelemetry("ins-104", new BigDecimal("0"));
        assertEquals(InstructionState.COMPLETED, instruction.getState());
        assertEquals(1, downlinkLog.size());
    }

    @Test
    void 验收5_平台重启后在途指令恢复超时检查() throws InterruptedException {
        DispatchInstruction instruction = newInstruction("ins-105");
        service.send(instruction);   // 发送前已持久化到仓库

        // 模拟平台重启：服务与补偿任务全部重建，仓库（持久层）保留
        InstructionService restartedService = new InstructionService(
                repository, downlinkLog::add, TOLERANCE_KW, STABLE_WINDOW_MS);
        TimeoutCompensationTask restartedTask = new TimeoutCompensationTask(
                repository, restartedService, ACK_TIMEOUT_MS, EXEC_TIMEOUT_MS);

        List<DispatchInstruction> recovered = restartedService.recoverInFlight();
        assertEquals(1, recovered.size(), "重启后必须恢复在途指令");
        assertEquals("ins-105", recovered.get(0).getInstructionId());

        Thread.sleep(ACK_TIMEOUT_MS + 30);
        assertEquals(1, restartedTask.runOnce(), "恢复的在途指令继续接受超时检查");
        assertEquals(InstructionState.REVIEW, instruction.getState());
    }

    @Test
    void 乱序回执快进_ACK丢失但设备直接报动作() {
        DispatchInstruction instruction = newInstruction("ins-106");
        service.send(instruction);
        service.onActStarted("ins-106");   // ACK 丢失，动作上报先到
        assertEquals(InstructionState.ACTING, instruction.getState(),
                "乱序回执应快进补齐 ACK，不判异常");
        assertNotNull(instruction.getAckedAt(), "缺失的 ACK 时标以快进时刻补齐");
    }

    @Test
    void 人工接管取消优先_取消后迟到回执不激活() {
        DispatchInstruction instruction = newInstruction("ins-107");
        service.send(instruction);
        service.cancel("ins-107", "人工接管");
        assertEquals(InstructionState.CANCELLED, instruction.getState(),
                "人工接管优先级高于平台控制，任意非终态可被抢占");

        service.onAck("ins-107");   // 迟到回执
        assertEquals(InstructionState.CANCELLED, instruction.getState(),
                "取消后迟到消息不得重新激活执行");
    }

    @Test
    void 终态后同编号重发被幂等拒绝() throws InterruptedException {
        DispatchInstruction instruction = newInstruction("ins-110");
        service.send(instruction);
        telemetryStable("ins-110");
        assertEquals(InstructionState.COMPLETED, instruction.getState());

        // 同编号再次下发：终态后不得作为新指令执行（重发须新编号 + 关联原编号）
        service.send(newInstruction("ins-110"));
        assertEquals(1, downlinkLog.size(), "终态后同编号重发不得再投递下行通道");
        assertEquals(InstructionState.COMPLETED, instruction.getState());
    }

    @Test
    void 单点达标不完成() {
        DispatchInstruction instruction = newInstruction("ins-111");
        service.send(instruction);
        LocalDateTime t0 = LocalDateTime.now();
        service.onTelemetry("ins-111", new BigDecimal("-505"), t0);
        assertEquals(InstructionState.SENT, instruction.getState(),
                "单个带内遥测点只证明瞬时进带，不得判定连续稳定");
        assertNotNull(instruction.getReachedAt(), "首个带内点必须记录达标起点（响应时延口径）");
    }

    @Test
    void 大间隔两达标点不完成() {
        DispatchInstruction instruction = newInstruction("ins-112");
        service.send(instruction);
        LocalDateTime t0 = LocalDateTime.now();
        service.onTelemetry("ins-112", new BigDecimal("-505"), t0);
        // 间隔 15s > 缺口上限（默认采样周期 5s 的 2 倍）：遥测断链，连续性无法证明
        service.onTelemetry("ins-112", new BigDecimal("-505"), t0.plusSeconds(15));
        assertEquals(InstructionState.SENT, instruction.getState(),
                "相邻带内点间隔超上限视为断链，连续计数清零，不得判定完成");
        // 断链后重新累计：再补一个正常间隔的带内点才构成连续两点
        service.onTelemetry("ins-112", new BigDecimal("-505"), t0.plusSeconds(20));
        assertEquals(InstructionState.COMPLETED, instruction.getState());
    }

    @Test
    void 连续两点达标完成_窗口为零也需两个点() {
        // 稳定窗口为 0 的教学配置：仍需 2 个连续带内点（与装配注释"两次连续进带"一致）
        InstructionService zeroWindowService = new InstructionService(
                repository, downlinkLog::add, TOLERANCE_KW, 0L);
        DispatchInstruction instruction = newInstruction("ins-113");
        zeroWindowService.send(instruction);

        LocalDateTime t0 = LocalDateTime.now();
        zeroWindowService.onTelemetry("ins-113", new BigDecimal("-505"), t0);
        assertEquals(InstructionState.SENT, instruction.getState(),
                "窗口为 0 时首次进带即完成是缺陷：单点不得判定完成");

        zeroWindowService.onTelemetry("ins-113", new BigDecimal("-505"), t0.plusSeconds(5));
        assertEquals(InstructionState.COMPLETED, instruction.getState(),
                "2 个连续带内点（间隔在缺口上限内）方可判定连续稳定达标");
    }

    @Test
    void 未超时与已达标指令不被误补偿() throws InterruptedException {
        DispatchInstruction fast = newInstruction("ins-108");
        service.send(fast);
        telemetryStable("ins-108");   // 已达标，不在补偿范围

        DispatchInstruction slow = newInstruction("ins-109");
        service.send(slow);
        Thread.sleep(ACK_TIMEOUT_MS + 30);

        int reviewed = compensationTask.runOnce();
        assertEquals(1, reviewed, "只有超时未确认指令应转核查");
        assertEquals(InstructionState.COMPLETED, fast.getState(), "已达标指令不得被误补偿");
        assertEquals(InstructionState.REVIEW, slow.getState());
    }
}
