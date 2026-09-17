package com.openvpp.dispatch.compensation;

import com.openvpp.dispatch.instruction.DispatchInstruction;
import com.openvpp.dispatch.instruction.InstructionRepository;
import com.openvpp.dispatch.instruction.InstructionService;
import com.openvpp.dispatch.instruction.InstructionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 超时补偿任务 —— 调度指令链路的兜底机制。
 *
 * 双超时分别处理：
 * - 接收超时（SENT 超 ackTimeoutMs）：设备可能根本没收到；
 * - 执行超时（ACKED/ACTING 超 execTimeoutMs）：设备受理了但遥测迟迟不达标，
 *   可能是执行缓慢、执行失败，也可能已执行但遥测链路中断。
 *
 * 两种情况都只说明"没消息"，不说明"没执行"——因此一律转 REVIEW 核查，
 * 禁止直接判失败重发（那会把"其实已执行"放大为设备重复动作）。
 * 补调本身（换资源重发）是策略层职责，且须以 REVIEW 核查结论为前提。
 * 生产形态为 XXL-Job / Spring Scheduled 定时驱动，演示版单测手动触发。
 */
public class TimeoutCompensationTask {

    private static final Logger log = LoggerFactory.getLogger(TimeoutCompensationTask.class);

    private final InstructionRepository repository;
    private final InstructionService instructionService;
    private final long ackTimeoutMs;
    private final long execTimeoutMs;

    public TimeoutCompensationTask(InstructionRepository repository,
                                   InstructionService instructionService,
                                   long ackTimeoutMs, long execTimeoutMs) {
        this.repository = repository;
        this.instructionService = instructionService;
        this.ackTimeoutMs = ackTimeoutMs;
        this.execTimeoutMs = execTimeoutMs;
    }

    /** 一轮补偿：返回本轮转 REVIEW 核查的指令数 */
    public int runOnce() {
        int toReview = 0;
        for (DispatchInstruction instruction : repository.findAllInFlight()) {
            if (instruction.isAckExpired(ackTimeoutMs)) {
                instructionService.toReview(instruction.getInstructionId(),
                        "接收确认超时 >" + ackTimeoutMs + "ms");
                toReview++;
            } else if (instruction.isExecutionExpired(execTimeoutMs)) {
                instructionService.toReview(instruction.getInstructionId(),
                        "执行超时 >" + execTimeoutMs + "ms，遥测未达标");
                toReview++;
            }
        }
        if (toReview > 0) {
            log.warn("超时补偿：{} 条指令转 REVIEW 核查，禁止直接重发", toReview);
        }
        return toReview;
    }
}
