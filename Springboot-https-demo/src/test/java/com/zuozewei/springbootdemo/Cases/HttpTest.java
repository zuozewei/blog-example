package com.zuozewei.springbootdemo.Cases;

import com.alibaba.fastjson.JSONObject;
import com.zuozewei.springbootdemo.util.HttpUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class HttpTest {

	@Test(enabled = false,description = "测试绕过SSL证书Post方法")
	public void doIgnoreVerifySSLPostTest() throws IOException, NoSuchAlgorithmException, KeyManagementException {
		String url = "https://localhost/springboot/person";
		//装填参数
		JSONObject param = new JSONObject();
		param.put("name","doIgnoreVerifySSLPost");
		param.put("age",20);
		//调用方法
		String response = HttpUtil.doIgnoreVerifySSLPost(url,param);
		//断言返回结果是否为空
		Assert.assertNotNull(response);
		System.out.println("【doIgnoreVerifySSLPost】"+response);
	}

	@Test(enabled = false,description = "测试绕过SSL证书Get方法")
	public void doIgnoreVerifySSLGetTest() throws IOException, NoSuchAlgorithmException, KeyManagementException {
		String url = "https://localhost/springboot/person";
		//调用方法
		String response = HttpUtil.doIgnoreVerifySSLGet(url,null);
		//断言返回结果是否为空
		Assert.assertNotNull(response);
		System.out.println("【doIgnoreVerifySSLGet】"+response);
	}

	@Test(enabled = true,description = "测试验证SSL证书Post方法")
	public void doVerifySSLPostTest() throws IOException {
		String url = "https://localhost/springboot/person";
		//装填参数
		JSONObject param = new JSONObject();
		param.put("name","doVerifySSLPost");
		param.put("age",20);
		//调用方法
		String response = HttpUtil.doVerifySSLPost(url,param);
		//断言返回结果是否为空
		Assert.assertNotNull(response);
		System.out.println("【doVerifySSLPost】"+response);
	}

	@Test(enabled = true,description = "测试验证SSL证书Get方法")
	public void doVerifySSLGetTest() throws IOException {
		String url = "https://localhost/springboot/person";
		//调用方法
		String response = HttpUtil.doVerifySSLGet(url,null);
		//断言返回结果是否为空
		Assert.assertNotNull(response);
		System.out.println("【doVerifySSLGet】"+response);
	}


}
