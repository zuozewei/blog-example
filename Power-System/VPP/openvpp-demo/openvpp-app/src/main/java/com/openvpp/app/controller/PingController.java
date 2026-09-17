package com.openvpp.app.controller;

import com.openvpp.common.api.ApiResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 骨架自检接口：确认单体启动与统一返回体生效。
 */
@RestController
@RequestMapping("/api/v1/system")
public class PingController {

    @GetMapping("/ping")
    public ApiResult<Map<String, Object>> ping() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("service", "openvpp-demo");
        payload.put("status", "UP");
        payload.put("columnPart", "part1-cognition");
        return ApiResult.ok(payload);
    }
}
