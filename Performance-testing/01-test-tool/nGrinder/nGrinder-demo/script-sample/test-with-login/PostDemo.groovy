import HTTPClient.Cookie
import HTTPClient.CookieModule
import HTTPClient.HTTPResponse
import HTTPClient.NVPair
import net.grinder.plugin.http.HTTPPluginControl
import net.grinder.plugin.http.HTTPRequest
import net.grinder.script.GTest
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static net.grinder.script.Grinder.grinder
import static org.hamcrest.Matchers.is

// import static net.grinder.util.GrinderUtils.* // You can use this if you're using nGrinder after 3.2.3

import static org.junit.Assert.assertThat

/**
 * @Title: PostDemo
 * @Description: post请求与json请求
 * @author 7d
 * @date 2019/10/24 / 12:22
 */

@RunWith(GrinderRunner)
class PostDemo {

    public static GTest test
    public static HTTPRequest request
    public static NVPair[] headers = []
    public static NVPair[] params = []
    public static Cookie[] cookies = []


    @BeforeProcess
    public static void beforeProcess() {
        HTTPPluginControl.getConnectionDefaults().timeout = 6000
        test = new GTest(1, "localhost:8888")
        request = new HTTPRequest()
        // Set header datas
        List<NVPair> headerList = new ArrayList<NVPair>()
        //POST key/value格式的params
//        headerList.add(new NVPair("Content-Type", "application/x-www-form-urlencoded"))

        headerList.add(new NVPair("Content-Type", "application/json"))

        headers = headerList.toArray()
        // Set param datas
//        List<NVPair> paramList = new ArrayList<NVPair>()
//        paramList.add(new NVPair("username", "600128"))
//
//        params = paramList.toArray()
//        // Set cookie datas
//        List<Cookie> cookieList = new ArrayList<Cookie>()
//
//        cookieList.add(new Cookie("Cookie","JSESSIONID=801DE60D9A00C391F80ABE0CD94A6E25","localhost:8888", "", new Date(), true))
//
//        cookies = cookieList.toArray()
        grinder.logger.info("before process.");

    }

    @BeforeThread
    public void beforeThread() {
        test.record(this, "test")
        grinder.statistics.delayReports = true;
        grinder.logger.info("before thread.");
    }

    @Before
    public void before() {
        request.setHeaders(headers)
        cookies.each { CookieModule.addCookie(it, HTTPPluginControl.getThreadHTTPClientContext()) }
        grinder.logger.info("before thread. init headers and cookies");

    }

    @Test
    public void test() {
        String url = "http://localhost:8888/findinfoJson"

        String parambody = "{\"username\":\"600128\"}"

        HTTPResponse result = request.POST(url, parambody.getBytes())
//        HTTPResponse result = request.GET("http://localhost:8888/findinfo", params)
        def text = result.getText()

        grinder.logger.info(text)

        assertThat(result.statusCode, is(200))


    }

}
