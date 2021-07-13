import static net.grinder.script.Grinder.grinder
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import net.grinder.plugin.http.HTTPRequest
import net.grinder.plugin.http.HTTPPluginControl
import net.grinder.script.GTest
import net.grinder.script.Grinder
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

import java.util.Date
import java.util.List
import java.util.ArrayList

import HTTPClient.Cookie
import HTTPClient.CookieModule
import HTTPClient.HTTPResponse
import HTTPClient.NVPair

/**
 * @Title: PostGetDemo
 * @Description: get请求
 * @author 7d
 * @date 2019/10/23 / 16:22
 */
@RunWith(GrinderRunner)
class PostGetDemo {

    public static GTest test
    // 定义 HTTPRequest 静态变量 request，用于发送 HTTP 请求
    public static HTTPRequest request
    // 定义 NVPair 数组 headers ，用于存放通用的请求头数据
    public static NVPair[] headers = []
    // 定义 NVPair 数组 params ，用于存放请求参数数据
    public static NVPair[] params = []
    // 定义 Cookie 数组 cookies ，用于存放通用的 cookie 数据
    public static Cookie[] cookies = []


    @BeforeProcess
    public static void beforeProcess() {
        // 设置请求响应超时时间（ms）
        HTTPPluginControl.getConnectionDefaults().timeout = 6000
        // 创建GTest对象，第一个参数1代表有多个请求/事务时的执行顺序ID,第二个参数是请求/事务的名称，会显示在summary结果中,有多个请求/事务时，要创建多个GTest对象
        test = new GTest(1, "localhost:8888")
        //创建 HTTPRequest 对象，用于发起 HTTP 请求
        request = new HTTPRequest()
        // Set header datas
        List<NVPair> headerList = new ArrayList<NVPair>()
        headerList.add(new NVPair("Content-Type", "application/x-www-form-urlencoded"))
        headerList.add(new NVPair("Connection", "keep-alive"))
        headers = headerList.toArray()
        // Set param datas
        List<NVPair> paramList = new ArrayList<NVPair>()
        paramList.add(new NVPair("username", "600128"))

        params = paramList.toArray()
        // Set cookie datas
        List<Cookie> cookieList = new ArrayList<Cookie>()
        cookieList.add(new Cookie("Cookie", "Idea-96adee05=87213429-7753-4183-99d0-c1c3362ce5d0; Hm_lvt_eb54a8c74fe4cb91ad8ca004e48cd3f9=1545999605; Pycharm-5b892e29=6772acb9-e87b-4d37-98e3-bff82e412d17; csrftoken=KDr8oYtD0gKh7UbS5fWSROzEOX6rjX3CN26pz9PQu9AczLMGFhw53ZrjAhoIOTE7; Hm_lvt_f2c884fc06fca522c4105429259b8a73=1558506687; Webstorm-173930bd=052a6829-b802-4eb0-a1e2-e348f1012604; _ga=GA1.1.1061327805.1562753587; UM_distinctid=16c02a6b049e1-02b6b032525db4-e343166-144000-16c02a6b04a120; CNZZDATA4617777=cnzz_eid%3D1637582848-1563412391-%26ntime%3D1563412391; Hm_lvt_0cb375a2e834821b74efffa6c71ee607=1563412574; bad_id22bdcd10-6250-11e8-917f-9fb8db4dc43c=a32ab941-a8f9-11e9-9989-93f7b4f25032; Idea-2b3f02ca=87213429-7753-4183-99d0-c1c3362ce5d0; cookie_lang=0; JSESSIONID=801DE60D9A00C391F80ABE0CD94A6E25", "localhost:8888", "", new Date(), true))

        cookies = cookieList.toArray()
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
//        发送GET请求
        HTTPResponse result = request.GET("http://localhost:8888/findinfo", params)
        def text = result.getText()

        grinder.logger.info(text)
// 断言HTTP请求状态码
        assertThat(result.statusCode, is(200))
    }

}
