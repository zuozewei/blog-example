import static net.grinder.script.Grinder.grinder;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import net.grinder.plugin.http.HTTPRequest;
import net.grinder.plugin.http.HTTPPluginControl;
import net.grinder.script.GTest;
import net.grinder.script.Grinder;
import net.grinder.scriptengine.groovy.junit.GrinderRunner;
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess;
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import HTTPClient.Cookie;
import HTTPClient.CookieModule;
import HTTPClient.HTTPResponse;
import HTTPClient.NVPair;

/**
 * @Title: TestPreDemo
 * @Description: demo
 * @author： 7d
 * @date 2019/10/23 / 13:09
 * http://ju.outofmemory.cn/entry/197566
 */


@RunWith(GrinderRunner)
class TestPreDemo {

    public static GTest test
    public static HTTPRequest request
    public static NVPair[] headers = []
    public static NVPair[] params = []
    public static Cookie[] cookies = []
    public static GTest test1
    public static GTest test2
    public static GTest test3
    public static GTest test4
    public static GTest test5


    @BeforeProcess
    public static void beforeProcess() {
        HTTPPluginControl.getConnectionDefaults().timeout = 6000
        // 实例化五个GTest对象
        test1 = new GTest(1, "test1")
        test2 = new GTest(2, "test2")
        test3 = new GTest(3, "test3")
        test4 = new GTest(4, "test4")
        test5 = new GTest(5, "test5")
        request = new HTTPRequest()
        grinder.logger.info("before process.");
    }

    @BeforeThread
    public void beforeThread() {
        // 设置五个请求的统计测试结果
        test1.record(this, "test1")
        test2.record(this, "test2")
        test3.record(this, "test3")
        test4.record(this, "test4")
        test5.record(this, "test5")
        grinder.statistics.delayReports = true;
        grinder.logger.info("before thread.");
    }

    @Before
    public void before() {
        request.setHeaders(headers)
        cookies.each {
            CookieModule.addCookie(it, HTTPPluginControl.getThreadHTTPClientContext())
        }
        grinder.logger.info("before thread. init headers and cookies");
    }

    @Test
    public void test() {
        // 获取总虚拟用户和运行线程数
        int vusers = getVusers()
        int runThreadNum = getRunThreadNum()

        // 运行百分比例设置
        int runRate1 = 30
        int runRate2 = 30
        int runRate3 = 10
        int runRate4 = 10
        int runRate5 = 10

        // 计算线程运行比例数
        int runNum1 = vusers / 100 * runRate1
        int runNum2 = vusers / 100 * (runRate1 + runRate2)
        int runNum3 = vusers / 100 * (runRate1 + runRate2 + runRate3)
        int runNum4 = vusers / 100 * (100 - runRate5)
        int runNum5 = vusers / 100 * 100

        // 根据比例进行相应请求
        if (runThreadNum > 0 && runThreadNum <= runNum1)
            this.test1()
        else if (runThreadNum > runNum1 && runThreadNum <= runNum2)
            this.test2()
        else if (runThreadNum > runNum2 && runThreadNum <= runNum3)
            this.test3()
        else if (runThreadNum > runNum3 && runThreadNum <= runNum4)
            this.test4()
        else if (runThreadNum > runNum4 && runThreadNum <= runNum5)
            this.test5()
    }

    public void test1() {
        grinder.logger.info("---请求一ones: {}---", grinder.threadNumber + 1)

    }

    public void test2() {
        grinder.logger.info("---请求二twos: {}---", grinder.threadNumber + 1)

    }

    public void test3() {
        grinder.logger.info("---请求三threes: {}---", grinder.threadNumber + 1)

    }

    public void test4() {
        grinder.logger.info("---请求四fours: {}---", grinder.threadNumber + 1)

    }

    public void test5() {
        grinder.logger.info("---请求五fives: {}---", grinder.threadNumber + 1)

    }

    public int getVusers() {
        // 获取虚拟用户总数
        int totalAgents = Integer.parseInt(grinder.getProperties().get("grinder.agents").toString())
        int totalProcesses = Integer.parseInt(grinder.properties.get("grinder.processes").toString())
        int totalThreads = Integer.parseInt(grinder.properties.get("grinder.threads").toString())
        int vusers = totalAgents * totalProcesses * totalThreads
        return vusers
    }

    public int getRunThreadNum() {
        // 获取当前压力机数、进程数、线程数
        int agentNum = grinder.agentNumber
        int processNum = grinder.processNumber
        int threadNum = grinder.threadNumber

        // 获取当前线程数
        int runThreadNum = (agentNum + 1) * (processNum + 1) * (threadNum + 1)
        return runThreadNum
    }
}
