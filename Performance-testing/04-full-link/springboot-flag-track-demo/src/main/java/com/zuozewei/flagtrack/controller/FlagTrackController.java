package com.zuozewei.flagtrack.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:  测试日志追踪
 * @Param:
 * @return:
 * @Author: zuozewei
 * @Date: 2021/3/26
 */

@Slf4j
@RestController
@RequestMapping("/test")
public class FlagTrackController {

    @GetMapping("/log")
    public String flagTrack(){
        log.info("-----> 测试 info <-----");
        log.warn("-----> 测试 warn <-----");
        log.error("-----> 测试 error <-----");
        return null;
    }

}