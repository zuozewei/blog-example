package com.zuozewei.extentreportdemo.testCase;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

public class TestMethodsDemo {

    @Test
    public void test1(){
        Assert.assertEquals(1,2);
    }

    @Test
    public void test2(){
        Assert.assertEquals(1,1);
    }


    @Test
    public void test3(){
        Assert.assertEquals("aaa","aaa");
    }


    @Test
    public void logDemo(){
        Reporter.log("这是故意写入的日志");
        throw new RuntimeException("故意运行时异常");
    }


}
