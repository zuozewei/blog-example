package com.openvpp.dispatch.instruction;

import java.util.List;
import java.util.Optional;

/**
 * 指令仓库 —— 指令全生命周期的唯一权威存储接口。
 *
 * 教学实现为内存 Map（{@link InMemoryInstructionRepository}）；
 * 生产形态必须落库（数据库表 + 状态索引），并在指令发送前完成持久化、
 * 与消息投递组成可靠投递链路——否则"数据库成功、消息发送失败"
 * 或平台重启都会导致在途指令永久失联。
 */
public interface InstructionRepository {

    void save(DispatchInstruction instruction);

    Optional<DispatchInstruction> find(String instructionId);

    default DispatchInstruction require(String instructionId) {
        return find(instructionId).orElseThrow(() ->
                new IllegalArgumentException("未登记的指令: " + instructionId));
    }

    /** 在途指令扫描入口（超时补偿与重启恢复的扫描集）：SENT / ACKED / ACTING / REVIEW */
    List<DispatchInstruction> findAllInFlight();

    List<DispatchInstruction> findByResource(String resourceId);

    /**
     * 清空全部指令（演示重置用）。
     * 生产形态无此入口——指令是全生命周期权威记录，只能走状态机终态，不得物理清除。
     */
    default void clear() {
        throw new UnsupportedOperationException("该实现不支持清空指令仓库");
    }
}
