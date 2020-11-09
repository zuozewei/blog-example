package com.zuozewei.demo.example;

import cn.hutool.core.lang.Singleton;
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


/**
 * 描述:
 * 获取会员信息接口
 *
 * @author zuozewei
 * @create 2020-11-7 15:26
 */

@SpringBootTest
@Slf4j
public class TestCase2 extends AbstractTestNGSpringContextTests {

    private String host = "cloud-gateway.mall.demo.7d.com";

    @Test( description = "获取会员信息")
    public void testInfo() {
        AssicateParam assicateParam = Singleton.get(AssicateParam.class);

        //打印
        log.info(assicateParam.getKey() +" : "+ assicateParam.getValue());

        //构造参数
        String url = "/mall-portal/sso/info";
        //关联参数
        String token = assicateParam.getValue();

        //链式构建请求
        HttpResponse res  = HttpRequest.get(host + url)
                .header(Header.CONTENT_TYPE, "application/x-www-form-urlencoded") //头信息，多个头信息多次调用此方法即可
                .header(Header.AUTHORIZATION,"Bearer " + token.toString())
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
    }

}




