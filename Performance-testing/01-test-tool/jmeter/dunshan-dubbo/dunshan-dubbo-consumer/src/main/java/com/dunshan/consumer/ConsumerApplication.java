package com.dunshan.consumer;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author 7DGroup
 * @version 1.0
 * @Date: 2021-05-04 11:57
 * @Description: 启动类
 */

@EnableDubbo //开启dubbo的注解支持
@SpringBootApplication
public class ConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}
