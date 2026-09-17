package com.openvpp.dispatch.instruction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

/**
 * 指令服务 —— 状态机迁移的唯一入口。
 *
 * 关键设计：
 * 1. 先持久化后投递：send() 先落仓库再投递下行通道，生产形态以发件箱
 *    （本地消息表 + 异步投递）消除"数据库成功、消息发送失败"的窗口期；
 * 2. 响应判定只信遥测：ACK 只证明"设备收到并接受"，COMPLETED 只能由
 *    至少 2 个连续带内遥测点驱动（相邻点间隔不超过缺口上限，间隔以遥测
 *    时间戳计），响应时延 = 遥测达标时刻 - 发送时刻；
 * 3. 回执确定性规则：重复回执幂等忽略；乱序回执快进补齐（ACK 丢失但
 *    设备已动作/已达标时以遥测与后续事件为准）；终态后迟到消息一律忽略，
 *    不得重新激活执行；
 * 4. 超时转核查而非判失败：接收超时与执行超时分别用独立阈值处理，
 *    超时且执行结果未知一律转 REVIEW，禁止直接重发（设备可能已执行）；
 * 5. 控制优先级：人工接管 > 本地保护 > 平台控制，任意非终态可被 cancel 抢占。
 */
public class InstructionService {

    private static final Logger log = LoggerFactory.getLogger(InstructionService.class);

    private final InstructionRepository repository;
    /** 下行通道：指令投递到设备侧的回调（生产形态替换为 MQ 发送 + 发件箱） */
    private final Consumer<DispatchInstruction> downlink;
    /** 遥测达标容差（kW）：|实测 - 目标| ≤ 容差视为进入容差带 */
    private final BigDecimal toleranceKw;
    /** 连续稳定窗口（毫秒）：进入容差带后须持续满足该时长才算响应成功 */
    private final long stableWindowMs;

    public InstructionService(InstructionRepository repository,
                              Consumer<DispatchInstruction> downlink,
                              BigDecimal toleranceKw, long stableWindowMs) {
        this.repository = repository;
        this.downlink = downlink;
        this.toleranceKw = toleranceKw;
        this.stableWindowMs = stableWindowMs;
    }

    /**
     * 创建并下发：CREATED → SENT。
     * 先持久化再投递下行通道；同 ID 指令幂等拦截覆盖全部已登记状态——
     * 在途重复下发不重复投递（重复投递会导致设备重复动作），
     * 终态（COMPLETED/FAILED/CANCELLED）后同编号同样拒绝作为新指令执行：
     * 设备侧以业务指令编号去重，终态后再收到同编号指令会触发设备重复动作。
     * 确需重发的场景（核查确认未执行后人工重发）必须使用新编号并关联原编号，
     * 由业务层显式建立"原指令 → 补发指令"的关系链，教学实现只做拒绝。
     */
    public void send(DispatchInstruction instruction) {
        if (repository.find(instruction.getInstructionId()).isPresent()) {
            log.warn("指令 {} 已登记（当前 {}），重复/终态后下发被幂等拦截",
                    instruction.getInstructionId(),
                    repository.find(instruction.getInstructionId())
                            .map(i -> i.getState().name()).orElse("UNKNOWN"));
            return;
        }
        repository.save(instruction);
        instruction.transitTo(InstructionState.SENT);
        downlink.accept(instruction);   // 投递下行通道，不等待回执
        log.info("指令已下发: {} → {} {}kW", instruction.getInstructionId(),
                instruction.getResourceId(), instruction.getCommandKw());
    }

    /**
     * 设备接收确认（ACK）：SENT → ACKED。
     * 只证明"设备收到并接受"，不是响应成功；重复/迟到 ACK 幂等忽略。
     */
    public void onAck(String instructionId) {
        DispatchInstruction instruction = repository.require(instructionId);
        switch (instruction.getState()) {
            case SENT:
                instruction.transitTo(InstructionState.ACKED);
                log.info("指令已确认: {} 通信耗时 {}ms（仅通信指标，不作响应时延）",
                        instructionId, instruction.ackElapsedMs());
                break;
            case ACKED:
            case ACTING:
                log.info("重复 ACK 忽略（幂等）: {} 当前 {}", instructionId, instruction.getState());
                break;
            case REVIEW:
                log.info("核查中收到迟到 ACK: {} 只证明设备曾收到，响应判定仍以遥测为准", instructionId);
                break;
            default:
                log.warn("终态后迟到 ACK 忽略: {} 当前 {}", instructionId, instruction.getState());
        }
    }

    /**
     * 设备开始动作：ACKED → ACTING。
     * SENT 态直收视为 ACK 丢失的乱序回执：快进补齐 ACKED，不判异常。
     */
    public void onActStarted(String instructionId) {
        DispatchInstruction instruction = repository.require(instructionId);
        switch (instruction.getState()) {
            case SENT:
                log.info("ACK 丢失但设备已动作（乱序回执快进）: {}", instructionId);
                instruction.transitTo(InstructionState.ACKED);
                instruction.transitTo(InstructionState.ACTING);
                break;
            case ACKED:
                instruction.transitTo(InstructionState.ACTING);
                log.info("设备开始动作: {}", instructionId);
                break;
            case ACTING:
                log.info("重复动作上报忽略（幂等）: {}", instructionId);
                break;
            case REVIEW:
                log.info("核查中收到动作上报: {} 佐证设备曾执行，响应判定仍以遥测为准", instructionId);
                break;
            default:
                log.warn("终态后迟到动作上报忽略: {} 当前 {}", instructionId, instruction.getState());
        }
    }

    /**
     * 遥测驱动 —— 响应成功与响应时延的唯一判据。
     * 实测功率进入容差带记录 reachedAt，至少 2 个连续带内遥测点判定达标转
     * COMPLETED；REVIEW 中遥测达标同样转 COMPLETED（迟到遥测可终结核查）。
     * 终态/未下发的迟到遥测一律忽略。
     */
    public void onTelemetry(String instructionId, BigDecimal measuredKw) {
        onTelemetry(instructionId, measuredKw, LocalDateTime.now());
    }

    /**
     * 携带遥测时间戳的遥测驱动：达标判据基于遥测点自带的时间戳，
     * 模拟场景传模拟时间可保留模拟时间语义（不随 wall-clock 漂移）。
     */
    public void onTelemetry(String instructionId, BigDecimal measuredKw, LocalDateTime observedAt) {
        DispatchInstruction instruction = repository.require(instructionId);
        InstructionState state = instruction.getState();
        if (state == InstructionState.CREATED || state.isTerminal()) {
            log.warn("迟到/无效遥测忽略: {} 当前 {}", instructionId, state);
            return;
        }
        if (instruction.evaluateTelemetry(measuredKw, toleranceKw, stableWindowMs, observedAt)) {
            instruction.completeByTelemetry();
            log.info("遥测连续稳定达标: {} 响应时延 {}ms", instructionId,
                    instruction.responseTimeMs());
        }
    }

    /**
     * 超时转核查：执行结果未知（设备可能已执行、只是回执/遥测链路断了），
     * 禁止简单判失败并重新下发——那会放大为设备重复动作。
     */
    public void toReview(String instructionId, String reason) {
        DispatchInstruction instruction = repository.require(instructionId);
        instruction.transitTo(InstructionState.REVIEW);
        log.warn("指令转核查: {} 原因: {}（不自动重发，待遥测佐证或人工确认）",
                instructionId, reason);
    }

    /** 确定的失败：设备明确拒绝，或核查确认未执行后人工判失败 */
    public void onFailed(String instructionId, String reason) {
        DispatchInstruction instruction = repository.require(instructionId);
        if (instruction.getState().isTerminal()) {
            log.warn("终态后失败上报忽略: {} 当前 {}", instructionId, instruction.getState());
            return;
        }
        instruction.transitTo(InstructionState.FAILED);
        log.warn("指令失败: {} 原因: {}", instructionId, reason);
    }

    /** 取消：人工接管 / 本地保护 / 平台撤销，任意非终态可被抢占 */
    public void cancel(String instructionId, String reason) {
        DispatchInstruction instruction = repository.require(instructionId);
        if (instruction.getState().isTerminal()) {
            log.warn("终态后取消忽略: {} 当前 {}", instructionId, instruction.getState());
            return;
        }
        instruction.transitTo(InstructionState.CANCELLED);
        log.warn("指令已取消: {} 原因: {}（控制权优先级：人工接管 > 本地保护 > 平台控制）",
                instructionId, reason);
    }

    /**
     * 重启恢复：扫描在途指令并恢复超时检查（返回的清单由补偿任务继续管辖）。
     * 教学实现与内存仓库配合演示恢复逻辑本身；生产形态为启动时
     * SELECT 在途状态指令（状态索引）重建定时检查，配合发件箱重投
     * 未投递成功的指令，消除"数据库成功、消息发送失败"的缺口。
     */
    public List<DispatchInstruction> recoverInFlight() {
        List<DispatchInstruction> inFlight = repository.findAllInFlight();
        inFlight.forEach(i -> log.info("恢复在途指令: {} 状态 {}", i.getInstructionId(), i.getState()));
        return inFlight;
    }
}
