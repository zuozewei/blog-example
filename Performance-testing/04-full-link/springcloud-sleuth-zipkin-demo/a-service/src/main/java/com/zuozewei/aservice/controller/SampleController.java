package com.zuozewei.aservice.controller;

import com.zuozewei.aservice.feignclient.BServiceFeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class SampleController {
    @Resource
    private BServiceFeignClient bService;

    @GetMapping("/a")
    public String methodA(){
        String result = bService.methodB();
        result = "-> Service A" + result;
        return result;
    }
}
