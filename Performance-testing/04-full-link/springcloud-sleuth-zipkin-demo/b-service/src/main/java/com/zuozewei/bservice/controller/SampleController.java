package com.zuozewei.bservice.controller;

import com.zuozewei.bservice.feignclient.CServiceFeignClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
public class SampleController {
    @Resource
    private CServiceFeignClient cService;
    @GetMapping("/b")
    @ResponseBody
    public String methodB(){
        String result = cService.methodC();
        result = " -> Service B" + result;
        return result;
    }
}
