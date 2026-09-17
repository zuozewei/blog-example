package com.openvpp.app.config;

import com.openvpp.dispatch.instruction.InMemoryInstructionRepository;
import com.openvpp.dispatch.instruction.InstructionRepository;
import com.openvpp.dispatch.instruction.InstructionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * 指令链路装配：内存仓库 + 本地回环下行通道。
 * 教学形态：下行通道是本地模拟设备回环（读者零外部依赖）；
 * 生产形态：替换为 MQ 发送 + 发件箱，仓库落数据库表（见第 15 篇）。
 */
@Configuration
public class DispatchConfig {

    private static final Logger log = LoggerFactory.getLogger(DispatchConfig.class);

    @Bean
    public InstructionRepository instructionRepository() {
        return new InMemoryInstructionRepository();
    }

    @Bean
    public InstructionService instructionService(InstructionRepository repository) {
        return new InstructionService(
                repository,
                instruction -> log.info("[DOWNLINK-LOOP] 指令投递本地模拟设备: {} → {} {}kW",
                        instruction.getInstructionId(), instruction.getResourceId(),
                        instruction.getCommandKw()),
                BigDecimal.valueOf(5.0),   // 遥测达标容差 5 kW
                0L);                        // 教学：稳定窗口 0（两次连续进带即达标）
    }
}
