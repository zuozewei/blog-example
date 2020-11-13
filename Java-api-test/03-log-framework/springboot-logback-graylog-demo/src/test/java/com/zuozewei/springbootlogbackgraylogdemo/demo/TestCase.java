package com.zuozewei.springbootlogbackgraylogdemo.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@SpringBootTest
@Slf4j
public class TestCase extends AbstractTestNGSpringContextTests {

    @Test
    public void testDemo() {
        log.info("输出info");
        log.debug("输出debug");
        log.error("输出error");
    }

}
