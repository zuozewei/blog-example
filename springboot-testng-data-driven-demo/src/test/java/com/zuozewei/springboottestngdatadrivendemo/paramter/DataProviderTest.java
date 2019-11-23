package com.zuozewei.springboottestngdatadrivendemo.paramter;

import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Iterator;

/**
 * 描述:
 * DataProvider 测试类
 *
 * @author zuozewei
 * @create 2019-11-23 19:56
 */

@Slf4j
public class DataProviderTest {

    @DataProvider(name="data")
    public Object[][] providerData(){
        Object[][] objects = new Object[][]{
                {"zhangsan",10},
                {"lisi",20},
                {"wangwu",30}
        };

        return objects;
    }

    @DataProvider(name="methodData")
    public Object[][] methodDataTest(Method method){
        Object[][] result=null;

        if(method.getName().equals("test1")){
            result = new Object[][]{
                    {"zhangsan",20},
                    {"lisi",25}
            };
        }else if(method.getName().equals("test2")){
            result = new Object[][]{
                    {"wangwu",50},
                    {"zhaoliu",60}
            };
        }

        return result;
    }

    @Test(dataProvider = "data")
    public void testDataProvider(String name,int age){
        log.info("name = [{}] ; age = [{}]" ,name,age);
    }

    @Test(dataProvider = "methodData")
    public void test1(String name,int age){
        log.info("test111方法: name = [{}] ; age = [{}]" ,name,age);
    }

    @Test(dataProvider = "methodData")
    public void test2(String name,int age){
        log.info("test222方法: name = [{}] ; age = [{}]" ,name,age);
    }

    @DataProvider(name = "iterator")
    public Iterator<Object[]> iteratorDataProvider(){
        return new AccoutIterator();

    }

    @Test(dataProvider = "iterator")
    public void testcase2(String name){
       log.info(" name = [{}] " ,name);
    }

}
