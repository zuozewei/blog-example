package com.openvpp.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 专栏演示启动入口：单体内聚所有业务模块。
 * 扫描根包 com.openvpp，各业务模块的组件经包扫描自动装配。
 */
@SpringBootApplication(scanBasePackages = "com.openvpp")
public class OpenVppApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenVppApplication.class, args);
    }
}
