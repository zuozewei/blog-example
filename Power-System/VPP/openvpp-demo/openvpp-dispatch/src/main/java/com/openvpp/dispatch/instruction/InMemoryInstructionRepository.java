package com.openvpp.dispatch.instruction;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 指令仓库内存实现 —— 教学演示用。
 *
 * 生产形态替换为数据库表 + 状态索引（超时补偿与重启恢复都按"在途状态"
 * 扫表，索引必须建在状态字段上），并在 {@code send()} 投递下行通道之前
 * 完成持久化；配合发件箱（本地消息表 + 异步投递）解决
 * "数据库成功、消息发送失败"的窗口期不一致。
 */
public class InMemoryInstructionRepository implements InstructionRepository {

    private final Map<String, DispatchInstruction> instructions = new ConcurrentHashMap<>();

    @Override
    public void save(DispatchInstruction instruction) {
        instructions.put(instruction.getInstructionId(), instruction);
    }

    @Override
    public Optional<DispatchInstruction> find(String instructionId) {
        return Optional.ofNullable(instructions.get(instructionId));
    }

    @Override
    public List<DispatchInstruction> findAllInFlight() {
        return instructions.values().stream()
                .filter(i -> !i.getState().isTerminal()
                        && i.getState() != InstructionState.CREATED)
                .collect(Collectors.toList());
    }

    @Override
    public List<DispatchInstruction> findByResource(String resourceId) {
        return instructions.values().stream()
                .filter(i -> i.getResourceId().equals(resourceId))
                .collect(Collectors.toList());
    }
}
