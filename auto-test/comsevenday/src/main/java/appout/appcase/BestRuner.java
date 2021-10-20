package appout.appcase;

import appout.base.DriverBase;
import appout.utils.LogUtil;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import org.openqa.selenium.By;
import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author 7DGroup
 * @Title: LoginTest
 * @Description: 用例区域
 * @date 2019/11/21 / 10:06
 */

public class BestRuner {


    static AndroidDriver<AndroidElement> driver;

    public static AndroidDriver<AndroidElement> getDriver() {
        return driver;
    }
    /**
     * 初始化运行类
     *
     * @param udid
     * @param port
     * @throws Exception
     */
    @BeforeClass
    @Parameters({"udid", "port"})
    public void BeforeClass(String udid, String port) {
        Reporter.log("步骤1：启动appium与应用", true);
        LogUtil.info("---这是设备ID号-->" + udid);
        LogUtil.info("--这是运行端口--->" + port);
        //通过路径获取包名与APP_ACTIVITY
        String apk = "com.jingdong.app.mall_70502.apk";
        driver = DriverBase.initDriver(port, udid, apk, true);
        driver.manage().timeouts().implicitlyWait(80, TimeUnit.SECONDS);
    }

    @Test(description = "初始化登录启动", testName = "进入首页", priority = 0)
    public void T001() {
        LogUtil.info("启动");
        driver.findElement(By.id("com.jingdong.app.mall:id/mj")).click();
    }

}
