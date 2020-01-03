package com.zuozewei.springboot.test;

import com.zuozewei.springboot.model.Configurations1;
import com.zuozewei.springboot.model.Configurations2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * 描述:
 * 演示测试用例2
 *
 * @author zuozewei
 * @create 2020-01-03 11:02
 */

@SpringBootTest
@Slf4j
public class TestCase2 extends AbstractTestNGSpringContextTests {

    @Autowired
    private Configurations2 configurations;

    @BeforeClass
    public void beforeClass() {
        String host = configurations.getHost();
        String port = configurations.getPort();
        String url = host +":"+ port;
        log.info("URL：" + url );
    }

    @Test
    public void test(){
        log.info("TestCase run...");
    }

}
