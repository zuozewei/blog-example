package com.zuozewei.springbootdemo.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;

public class HttpUtil {

	/**
	 * 验证SSL证书，发送Post请求（Json形式）
	 * @param url
	 * @param param
	 * @return
	 * @throws IOException
	 */
	public static String doVerifySSLPost(String url, JSONObject param) throws IOException {
		//采用验证的SSL证书方式处理https请求
		SSLContext sslContext = SSLCustom("./src/main/resources/keystore.p12","zuozewei");
		final SSLConnectionSocketFactory sslsf;

		//设置协议http和https对应的处理socket链接工厂的对象
		sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
		final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", new PlainConnectionSocketFactory())
				.register("https", sslsf)
				.build();

		final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
		cm.setMaxTotal(100);

		//创建自定义的httpclient对象
		CloseableHttpClient httpClient = HttpClients.custom()
				.setSSLSocketFactory(sslsf)
				.setConnectionManager(cm)
				.build();

		String result = null;

		//创建post方式请求对象
		HttpPost httpPost = new HttpPost(url);
		//装填参数
		StringEntity entity = new StringEntity(param.toString(),"utf-8");
		entity.setContentEncoding("UTF-8");
		entity.setContentType("application/json");
		//设置参数到请求对象中
		httpPost.setEntity(entity);
		//执行请求操作，并拿到结果（同步阻塞）
		CloseableHttpResponse response = httpClient.execute(httpPost);
		if (response.getStatusLine().getStatusCode() == 200){
			//获取结果实体
			HttpEntity httpEntity = response.getEntity();
			//按指定编码转换结果实体为String类型
			result = EntityUtils.toString(httpEntity,"UTF-8");
		}
		//释放链接
		response.close();

		return result;
	}

	/**
	 * 验证SSL证书，发送Get请求
	 * @param url
	 * @param params
	 * @return
	 * @throws IOException
	 */
	public static String doVerifySSLGet(String url, Map<String,Object> params) throws IOException {
		//采用验证的SSL证书方式处理https请求
		SSLContext sslContext = SSLCustom("./src/main/resources/keystore.p12","zuozewei");
		final SSLConnectionSocketFactory sslsf;

		// 设置协议http和https对应的处理socket链接工厂的对象
		sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
		final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", new PlainConnectionSocketFactory())
				.register("https", sslsf)
				.build();

		final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
		cm.setMaxTotal(100);

		//创建自定义的httpclient对象
		CloseableHttpClient httpClient = HttpClients.custom()
				.setSSLSocketFactory(sslsf)
				.setConnectionManager(cm)
				.build();

		String result = null;
		//装填参数
		StringBuffer param = new StringBuffer();
		if (params != null && !params.isEmpty()) {
			int i = 0;
			for (String key : params.keySet()) {
				if (i == 0) {
					param.append("?");
				} else {
					param.append("&");
				}
				param.append(key).append("=").append(params.get(key));
				i++;
			}
			url += param;
		}

		//创建get方式请求对象
		HttpGet httpGet = new HttpGet(url);
		//执行请求操作，并拿到结果（同步阻塞）
		CloseableHttpResponse response = httpClient.execute(httpGet);
		if (response.getStatusLine().getStatusCode() == 200){
			//获取结果实体
			HttpEntity httpEntity = response.getEntity();
			//按指定编码转换结果实体为String类型
			result = EntityUtils.toString(httpEntity,"UTF-8");
		}

		//释放链接
		response.close();

		return result;
	}

	/**
	 * 绕过SSL证书，发送Get请求
	 * @param url
	 * @param params
	 * @return
	 * @throws IOException
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 */
	public static String doIgnoreVerifySSLGet(String url, Map<String,Object> params)
			throws IOException, KeyManagementException, NoSuchAlgorithmException {
		//采用绕过验证的方式处理https请求
		SSLContext sslContext = createIgnoreVerifySSL();
		final SSLConnectionSocketFactory sslsf;

		//设置协议http和https对应的处理socket链接工厂的对象
		sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
		final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", new PlainConnectionSocketFactory())
				.register("https", sslsf)
				.build();

		final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
		cm.setMaxTotal(100);

		//创建自定义的httpclient对象
		CloseableHttpClient httpClient = HttpClients.custom()
				.setSSLSocketFactory(sslsf)
				.setConnectionManager(cm)
				.build();

		String result = null;
		//装填参数
		StringBuffer param = new StringBuffer();
		if (params != null && !params.isEmpty()) {
			int i = 0;
			for (String key : params.keySet()) {
				if (i == 0) {
					param.append("?");
				} else {
					param.append("&");
				}
				param.append(key).append("=").append(params.get(key));
				i++;
			}
			url += param;
		}
		//创建get方式请求对象
		HttpGet httpGet = new HttpGet(url);
		//执行请求操作，并拿到结果（同步阻塞）
		CloseableHttpResponse response = httpClient.execute(httpGet);
		if (response.getStatusLine().getStatusCode() == 200){
			//获取结果实体
			HttpEntity httpEntity = response.getEntity();
			//按指定编码转换结果实体为String类型
			result = EntityUtils.toString(httpEntity,"UTF-8");
		}

		//释放链接
		response.close();

		return result;
	}

	/**
	 * 绕过SSL证书，发送Post请求（Json形式）
	 * @param url
	 * @param param
	 * @return
	 * @throws IOException
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 */
	public static String doIgnoreVerifySSLPost(String url, JSONObject param)
			throws IOException, KeyManagementException, NoSuchAlgorithmException {
		//采用绕过验证的方式处理https请求
		SSLContext sslContext = createIgnoreVerifySSL();
		final SSLConnectionSocketFactory sslsf;

		//设置协议http和https对应的处理socket链接工厂的对象
		sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
		final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", new PlainConnectionSocketFactory())
				.register("https", sslsf)
				.build();

		final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
		cm.setMaxTotal(100);

		//创建自定义的httpclient对象
		CloseableHttpClient httpClient = HttpClients.custom()
				.setSSLSocketFactory(sslsf)
				.setConnectionManager(cm)
				.build();

		String result = null;
		//创建post方式请求对象
		HttpPost httpPost = new HttpPost(url);
		//装填参数
		StringEntity entity = new StringEntity(param.toString(),"utf-8");
		entity.setContentEncoding("UTF-8");
		entity.setContentType("application/json");
		//设置参数到请求对象中
		httpPost.setEntity(entity);

		//执行请求操作，并拿到结果（同步阻塞）
		CloseableHttpResponse response = httpClient.execute(httpPost);
		if (response.getStatusLine().getStatusCode() == 200){
			//获取结果实体
			HttpEntity httpEntity = response.getEntity();
			//按指定编码转换结果实体为String类型
			result = EntityUtils.toString(httpEntity,"UTF-8");
		}

		//释放链接
		response.close();

		return result;
	}

	/**
	 * 绕过SSL验证
	 *
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sslContext = SSLContext.getInstance("SSLv3");

		// 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
		X509TrustManager trustManager = new X509TrustManager() {
			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
					String paramString) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
					String paramString) throws CertificateException {
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};

		sslContext.init(null, new TrustManager[] { trustManager }, null);
		return sslContext;
	}


	/**
	 * 设置信任自签名证书
	 *
	 * @param keyStorePath 密钥库路径
	 * @param keyStorepass 密钥库密码
	 * @return
	 */
	public static SSLContext SSLCustom(String keyStorePath, String keyStorepass){
		SSLContext sslContext = null;
		FileInputStream instream = null;
		KeyStore trustStore = null;
		try {
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			instream = new FileInputStream(new File(keyStorePath));
			trustStore.load(instream, keyStorepass.toCharArray());
			// 相信自己的CA和所有自签名的证书
			sslContext = SSLContexts.custom().loadTrustMaterial(trustStore, new TrustSelfSignedStrategy()).build();
		} catch (KeyStoreException | NoSuchAlgorithmException| CertificateException | IOException | KeyManagementException e) {
			e.printStackTrace();
		} finally {
			try {
				instream.close();
			} catch (IOException e) {
			}
		}
		return sslContext;
	}

}
