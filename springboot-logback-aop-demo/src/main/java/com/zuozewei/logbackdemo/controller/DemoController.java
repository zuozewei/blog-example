package com.zuozewei.logbackdemo.controller;

import com.zuozewei.logbackdemo.config.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * aop统一异常示例
 * @author oKong
 *
 */
@RestController
@Slf4j
public class DemoController {

	/**
	 * http://127.0.0.1:8888/index/?content="我是测试内容"
	 *
	 * @param content
	 * @return
	 */
	@Log("请求了aopDemo方法")
	@GetMapping(value = "aop")
	public String aopDemo(@RequestParam String content) {
		LocalDateTime localDateTime = LocalDateTime.now();

		log.trace("请求参数：content:{}", content);
		log.debug("请求参数：content:{}", content);
		log.info("请求参数：content:{}", content);
		log.warn("请求参数：content:{}", content);
		log.error("请求参数：content:{}", content);

		return localDateTime + ",content:" + content;
	}

	/**
	 * 不拦截日志示例
	 * @param content
	 * @return
	 */
	@GetMapping(value = "/notaop")
	@Log(ignore=true)
	public String notAopDemo(String content) {
		LocalDateTime localDateTime = LocalDateTime.now();

		log.trace("请求参数：content:{}", content);
		log.debug("请求参数：content:{}", content);
		log.info("请求参数：content:{}", content);
		log.warn("请求参数：content:{}", content);
		log.error("请求参数：content:{}", content);

		return localDateTime + ",content:" + content;
	}
}
