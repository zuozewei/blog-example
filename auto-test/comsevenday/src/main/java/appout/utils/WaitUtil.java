package appout.utils;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Reporter;

/**
 * @author 7DGroup
 * @Title: WaitUtil
 * @Description: 等待查找元素工具类
 * @date 2019/11/23 / 10:58
 */

public class WaitUtil {

    public static void waitWebElement(AndroidDriver<AndroidElement> driver, By by, String name) {
        LogUtil.info("等待元素出现-->目前操步骤为：" + name);
        Reporter.log("目前操步骤为：" + name);
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.presenceOfElementLocated(by));
    }
}
