package com.zuozewei.aservice.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("b-service")
public interface BServiceFeignClient {
    @GetMapping("/b")
    public String methodB();
}
