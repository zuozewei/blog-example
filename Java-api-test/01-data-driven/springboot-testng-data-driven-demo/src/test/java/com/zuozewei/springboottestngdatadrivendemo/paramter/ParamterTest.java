package com.zuozewei.springboottestngdatadrivendemo.paramter;

import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * 描述:
 * testng.xml 参数测试类
 *
 * @author zuozewei
 * @create 2019-11-23 19:56
 */

@Slf4j
public class ParamterTest {

    @Test
    @Parameters({"name","age"})
    public void paramTest(String name,int age){
        log.info("name = [{}] ; age = [{}]" ,name,age);
    }

}
