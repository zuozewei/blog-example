import HTTPClient.Cookie
import HTTPClient.CookieModule
import HTTPClient.HTTPResponse
import HTTPClient.NVPair
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import groovy.json.JsonParser
import groovy.json.JsonSlurper
import net.grinder.plugin.http.HTTPPluginControl
import net.grinder.plugin.http.HTTPRequest
import net.grinder.script.GTest
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSON;

import static com.alibaba.fastjson.JSON.parse
import static net.grinder.script.Grinder.grinder
import static org.hamcrest.Matchers.is


// import static net.grinder.util.GrinderUtils.* // You can use this if you're using nGrinder after 3.2.3

import static org.junit.Assert.assertThat

/**
 * @Title: OneAndTwo
 * @author 7d
 * @date 2019/10/27 / 11:00
 */
@RunWith(GrinderRunner)
class OneAndTwo {

    public static GTest test
    // 定义 HTTPRequest 静态变量 request，用于发送 HTTP 请求
    public static HTTPRequest request
    // 定义 NVPair 数组 headers ，用于存放通用的请求头数据
    public static NVPair[] headers = []
    // 定义 NVPair 数组 params ，用于存放请求参数数据
    public static NVPair[] params = []
    // 定义 Cookie 数组 cookies ，用于存放通用的 cookie 数据
    public static Cookie[] cookies = []

    //存储第一个请求得参数
    def paramName = new ArrayList()


    @BeforeProcess
    public static void beforeProcess() {
        // 设置请求响应超时时间（ms）
        HTTPPluginControl.getConnectionDefaults().timeout = 6000
        // 创建GTest对象，第一个参数1代表有多个请求/事务时的执行顺序ID,
        // 第二个参数是请求/事务的名称，会显示在summary结果中,有多个请求/事务时，要创建多个GTest对象
        test = new GTest(1, "User_find_01")
        //创建 HTTPRequest 对象，用于发起 HTTP 请求
        request = new HTTPRequest()
        // Set header datas
        List<NVPair> headerList = new ArrayList<NVPair>()
        headerList.add(new NVPair("Content-Type", "application/x-www-form-urlencoded"))
        headerList.add(new NVPair("Connection", "keep-alive"))
        headers = headerList.toArray()
        // Set param datas

//        List<Cookie> cookieList = new ArrayList<Cookie>()
//        cookieList.add(new Cookie("Cookie", "null", "localhost:8888", "", new Date(), true))
//        cookies = cookieList.toArray()
        grinder.logger.info("before process.");

    }

    @BeforeThread
    public void beforeThread() {
//        注册事件，启动test，第二个参数要与@Test注解的方法名保持一致,有多个请求/事务时，要注册多个事件
        test.record(this, "test")

        //配置延迟报告统计结果
        grinder.statistics.delayReports = true;
        grinder.logger.info("before thread.");
    }

    @Before
    public void before() {
        //在这里可以添加headers属性和cookies
//        request.setHeaders(headers)
        cookies.each { CookieModule.addCookie(it, HTTPPluginControl.getThreadHTTPClientContext()) }
        grinder.logger.info("before thread. init headers and cookies");

    }

    @Test
    public void test() {
        getUserFind()
        getItem()
    }


    public void getUserFind() {
        // 发送GET请求
        HTTPResponse resu = request.GET("http://localhost:8888/userfind")
        def text1 = resu.getText()
        JSONObject jsonObject = JSONObject.parseObject(text1);
        JSONObject object = jsonObject.getJSONObject("extend")
        JSONArray array = object.getJSONArray("info")
        for (int i = 0; i < array.size(); i++) {
            JSONObject object1 = JSONObject.parseObject(array.get(i).toString())
            Object name = object1.get("userName");
            grinder.logger.info("这是获取得名字--->" + name)
            paramName.add(String.valueOf(name))
        }
        grinder.logger.info("这是第一个请求：" + text1)
        assertThat(resu.statusCode, is(200))
    }

    public void getItem() {
        List<NVPair> paramList = new ArrayList<NVPair>()
        //获取参数的第一个值
        paramList.add(new NVPair("userName", paramName.get(0)))
        params = paramList.toArray()
        // Set cookie datas
        HTTPResponse result = request.GET("http://localhost:8888/findName", params)
        def text = result.getText()

        grinder.logger.info("这是第二请求" + text)
        // 断言HTTP请求状态码
        assertThat(result.statusCode, is(200))
    }
}
