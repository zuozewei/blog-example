package com.dunshan.provider;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author 7DGroup
 * @version 1.0
 * @Date: 2021-05-04 12:02
 * @Description: 启动类
 */
@EnableDubbo //开启Dubbo的注解支持
@SpringBootApplication
public class ProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }
}
