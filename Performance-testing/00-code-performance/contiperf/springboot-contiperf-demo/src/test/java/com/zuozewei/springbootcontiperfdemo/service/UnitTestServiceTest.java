package com.zuozewei.springbootcontiperfdemo.service;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 编写接口性能测试类
 * @author zuozewei
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest //SpringBootTest 是springboot 用于测试的注解，可指定启动类或者测试环境等，这里直接默认。
public class UnitTestServiceTest {
	
	@Autowired
	UnitTestService testService;
	
	// 引入 ContiPerf 进行性能测试
	@Rule
	public ContiPerfRule contiPerfRule = new ContiPerfRule();

	@Test
	@PerfTest(invocations = 10000,threads = 100) 	//100个线程 执行10000次
	public void test() {
		String msg = "this is a test";
		String result = testService.process(msg);
		//断言 是否和预期一致
		Assert.assertEquals(msg,result);
	}
}
