package com.zuozewei.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 多环境配置
 * @author zuozewei
 *
 */

@SpringBootApplication
@Slf4j
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		log.info("多环境应用启动.");
	}

}
