package com.zuozewei.springboot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 
 * 启动方法
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
