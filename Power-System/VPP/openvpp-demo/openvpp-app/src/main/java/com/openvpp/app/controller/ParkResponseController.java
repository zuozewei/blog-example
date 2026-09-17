package com.openvpp.app.controller;

import com.openvpp.app.orchestration.DemoRunResult;
import com.openvpp.app.orchestration.ParkResponseOrchestrator;
import com.openvpp.app.persistence.ResponseRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 园区需求响应贯穿案例入口 —— 闭环触发 + 业务查询。
 *
 * 触发：POST /api/v1/demo/run?responseId=run-001&path=NORMAL
 * 查询：任务 /api/v1/tasks、指令 /api/v1/instructions、
 *       基线 /api/v1/baselines/{responseId}、账单 /api/v1/bills
 * 幂等：同一 responseId 重复 POST 不重复执行、不重复出账。
 * 重置：POST /api/v1/demo/reset（清理演示数据，便于再次运行）。
 */
@RestController
@RequestMapping("${openvpp.api-prefix:/api/v1}")
public class ParkResponseController {

    private final ParkResponseOrchestrator orchestrator;
    private final ResponseRepository repo;

    public ParkResponseController(ParkResponseOrchestrator orchestrator, ResponseRepository repo) {
        this.orchestrator = orchestrator;
        this.repo = repo;
    }

    /** 触发一遍闭环。path: NORMAL / DEGRADED / DISPUTED */
    @PostMapping("/demo/run")
    public DemoRunResult run(@RequestParam String responseId,
                             @RequestParam(defaultValue = "NORMAL") String path,
                             @RequestParam(defaultValue = "600") BigDecimal declaredKwh,
                             @RequestParam(defaultValue = "900") BigDecimal targetKw) {
        return orchestrator.run(responseId, path, declaredKwh, targetKw);
    }

    /** 清理演示数据（重置后可用同一 responseId 再次运行） */
    @PostMapping("/demo/reset")
    public Map<String, Object> reset() {
        int t = repo.deleteAll();
        return Map.of("cleared", t, "note", "演示数据已清理，可再次运行");
    }

    @GetMapping("/tasks")
    public List<Map<String, Object>> tasks() {
        return repo.listTasks();
    }

    @GetMapping("/instructions")
    public List<Map<String, Object>> instructions(@RequestParam(required = false) String responseId) {
        return repo.listInstructions(responseId);
    }

    @GetMapping("/baselines")
    public List<Map<String, Object>> baselines(@RequestParam String responseId) {
        return repo.listBaselines(responseId);
    }

    @GetMapping("/bills")
    public List<Map<String, Object>> bills(@RequestParam(required = false) String responseId) {
        return repo.listBills(responseId);
    }
}
