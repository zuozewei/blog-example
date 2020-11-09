package com.zuozewei.demo.example;

import cn.hutool.core.lang.Singleton;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.zuozewei.demo.entity.AssicateParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * 描述:
 * 会员登录接口测试
 *
 * @author zuozewei
 * @create 2020-11-7 15:26
 */

@SpringBootTest
@Slf4j
public class TestCase1 extends AbstractTestNGSpringContextTests {

    private String host = "cloud-gateway.mall.demo.7d.com";
    AssicateParam assicateParam = new AssicateParam();

//    @Test(description = "用例1：参数取值")
//    public void test1() {
//        Dog dog = new Dog();
//        dog.setName("大黄");
//        dog.setWeight(5.14f);
//
//        Singleton.put(dog);
//    }
//
//    @Test(description = "用例2：参数调用")
//    public void test2() {
//        Dog dog = Singleton.get(Dog.class);
//        //Bean转Map
//        Map<String, Object> map = BeanUtil.beanToMap(dog);
//        //打印
//        log.info("beanUtil bean to map:{}", map.get("name"));
//        log.info("beanUtil bean to map:{}", map.get("weight"));
//    }

    @Test(description = "登录")
    public void testLogin() {

        //构造参数
        String url = "/mall-portal/sso/login";
        //将多个参数对加入到Map中
        Map<String, Object> map = MapUtil.newHashMap();
        map.put("username", "lisi");   //存储key和value
        map.put("password", "123456");

        //链式构建请求
        HttpResponse res = HttpRequest.post(host + url)
                .header(Header.CONTENT_TYPE, "application/x-www-form-urlencoded") //头信息，多个头信息多次调用此方法即可
                .form(map)   //表单内容
                .timeout(20000)//超时，毫秒
                .execute();

        //获取响应体
        String result = res.body();
        //获取响应码
        int status = res.getStatus();

        //断言
        Assert.assertEquals(200,status);
        Assert.assertNotNull(result);

        //打印
        log.info(JSONUtil.formatJsonStr(result));
        log.info(JSONUtil.parseObj(result).getByPath("data.token").toString());

        //存储token
        assicateParam.setKey("token");
        assicateParam.setValue(JSONUtil.parseObj(result).getByPath("data.token").toString());
        Singleton.put(assicateParam);
    }

}




