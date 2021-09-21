package com.zuozewei.cservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {
    @GetMapping("/c")
    public String methodC(){
        String result = " -> Service C";
        return result;
    }
}
