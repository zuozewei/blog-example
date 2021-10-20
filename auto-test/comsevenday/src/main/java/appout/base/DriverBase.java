package appout.base;

import appout.utils.OperationalCmd;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.functions.ExpectedCondition;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.touch.LongPressOptions;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;

/**
 * @author 7DGroup
 * @Title: Bases
 * @Description: 安装初始化类
 * @date 2019/6/21 / 10:34
 */

public class DriverBase {
    private static final Logger log = LoggerFactory.getLogger(DriverBase.class);

    public static AndroidDriver<AndroidElement> driver;

    /**
     * @param port ：服务器启动的端口号，系统自动获取
     * @param udid :手机设备号:系统自动化获取
     * @param apk  :自动化运行的APK包，系统会根据该地址获取包名与actiber
     * @param flag ：true 卸掉有重新安装与运行后自动化卸掉包。false 直接安装运行
     * @return
     */
    public static AndroidDriver<AndroidElement> initDriver(String port, String udid, String apk, boolean flag) {
        ArrayList<String> packAct = OperationalCmd.getPackAct(apk);
        DesiredCapabilities caps = new DesiredCapabilities();
        //自动安装
        if (flag) {
            caps.setCapability(MobileCapabilityType.APP, apk);
            //结束后会卸载程序
            caps.setCapability(MobileCapabilityType.FULL_RESET, AndroidCapabilityType.FULL_RESET);
        }
        caps.setCapability(AndroidMobileCapabilityType.APPLICATION_NAME, udid);
        //PLATFORM_NAME: 平台名称
        caps.setCapability(AndroidMobileCapabilityType.PLATFORM_NAME, AndroidCapabilityType.PLATFORM_NAME);
        //  UDID:设置操作手机的唯一标识，android手机可以通过adb devices查看
        caps.setCapability(MobileCapabilityType.DEVICE_NAME, udid);
        caps.setCapability(MobileCapabilityType.APPIUM_VERSION, "4.4.2");
        //NEW_COMMAND_TIMEOUT: appium server和脚本之间的 session超时时间
        caps.setCapability(AndroidCapabilityType.NEW_COMMAND_TIMEOUT, AndroidCapabilityType.NEW_COMMAND_TIMEOUT);
        //APP_PACKAG：Android应用的包名
        caps.setCapability(AndroidMobileCapabilityType.APP_PACKAGE, packAct.get(0));
        //APP_ACTIVITY ：启动app的起始activity
        caps.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY, packAct.get(1));
        //UNICODE_KEYBOARD：1、中文输入不支持，2、不用它键盘会弹出来，说不定会影响下一步操作.需要注意设置后，需要将手机的输入法进行修改
        caps.setCapability(AndroidMobileCapabilityType.UNICODE_KEYBOARD, AndroidCapabilityType.UNICODE_KEY_BOARD);
        //Reset_KEYBOARD:是否重置输入法
        caps.setCapability(AndroidMobileCapabilityType.RESET_KEYBOARD, AndroidCapabilityType.RESET_KEY_BOARD);
        //NO_SIGN:跳过检查和对应用进行 debug 签名的
        caps.setCapability(AndroidMobileCapabilityType.NO_SIGN, AndroidCapabilityType.NO_SIGN);
        try {
            //appium测试服务的地址
            String serverUrl = "http://127.0.0.1";
            driver = new AndroidDriver<>(new URL(serverUrl + ":" + port + "/wd/hub"), caps);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return driver;
    }


    /**
     * 截图使用
     *
     * @return
     */
    public static AppiumDriver getDriver() {
        return driver;
    }

    /**
     * 总的滑动方法
     *
     * @param driver
     * @param directi up、down、left、right
     */
    public static void swipe(AndroidDriver<AndroidElement> driver, String directi) {
        switch (directi.toLowerCase()) {
            case "up":
                SwipeUp(driver);
                break;
            case "down":
                SwipeDown(driver);
                break;
            case "left":
                SwipeLeft(driver);
                break;
            case "right":
                SwipeRight(driver);
                break;
            default:
                System.out.println("方向参数不对，只能是up、down、left、right");
                break;
        }
    }

    /**
     * 上滑
     *
     * @param driver
     */
    public static void SwipeUp(AndroidDriver<AndroidElement> driver) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Dimension size = driver.manage().window().getSize();
        int height = size.height;
        int width = size.width;
        new TouchAction(driver).longPress(PointOption.point(width / 2, 100))
                .moveTo(PointOption.point(width / 2, height - 100)).release()
                .perform();
    }

    /**
     * 下滑
     *
     * @param driver
     */
    public static void SwipeDown(AndroidDriver<AndroidElement> driver) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Dimension size = driver.manage().window().getSize();
        int height = size.height;
        int width = size.width;
        new TouchAction(driver)
                .longPress(PointOption.point(width / 2, height - 100))
                .moveTo(PointOption.point(width / 2, 100)).release().perform();
    }

    /**
     * 左滑
     *
     * @param driver
     */
    public static void SwipeLeft(AndroidDriver<AndroidElement> driver) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Dimension size = driver.manage().window().getSize();
        int height = size.height;
        int width = size.width;
        new TouchAction(driver)
                .longPress(PointOption.point(width - 100, height / 2))
                .moveTo(PointOption.point(100, height / 2)).release().perform();
    }

    /**
     * 右滑
     *
     * @param driver
     */
    public static void SwipeRight(AndroidDriver<AndroidElement> driver) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Dimension size = driver.manage().window().getSize();
        int height = size.height;
        int width = size.width;
        new TouchAction(driver).longPress(PointOption.point(100, height / 2))
                .moveTo(PointOption.point(width - 100, height / 2)).release()
                .perform();
    }

    /**
     * @param element 要查找的元素
     * @param startX  x起始坐标
     * @param endX    x终点坐标
     * @param startY  y起始坐标
     * @param endY    y终点坐标
     * @Function 屏幕滑动至特定控件
     * @Author 泰国站
     */
    public static void swipePage(AndroidDriver<AndroidElement> driver, String element, double startX, double endX, double startY, double endY) {

        //获取手机屏幕大小
        Dimension screenSize = driver.manage().window().getSize();

        while (true) {
            try {
                (new TouchAction(driver))
                        .longPress(PointOption.point((int) (screenSize.width * startX), (int) (screenSize.height * startY)))
                        .moveTo(PointOption.point((int) (screenSize.width * endX), (int) (screenSize.height * endY)))
                        .release()
                        .perform();
            } catch (Exception e) {
                if (startX >= 1 || startX <= 0) {
                    System.out.println("超出手机屏幕边界，x起始坐标必须小于1且大于0");
                } else if (endX >= 1 || endX <= 0) {
                    System.out.println("超出手机屏幕边界，x终点坐标必须小于1且大于0");
                } else if (startY >= 1 || startY <= 0) {
                    System.out.println("超出手机屏幕边界，y起始坐标必须小于1且大于0");
                } else if (endY >= 1 || endY <= 0) {
                    System.out.println("超出手机屏幕边界，y终点坐标必须小于1且大于0");
                }
                return;
            }
            String pagesource = driver.getPageSource();
            //判断元素是否存在，存在则不等于-1,String.indexOf(xxxx)返回包含该字符串在父类字符串中起始位置，不包含则全部返回-1
            if (pagesource.indexOf(element) != -1) {
                return;
            }
        }
    }

    /**
     * @param element  要操作的控件元素属性
     * @param distance 要滑动的距离，1滑到头/2是滑2分之一／3是滑3分之一
     * @Function 控件内滑动
     * @Author 泰国站
     */
    public static void swipeCrosswiseControl(AndroidDriver<AndroidElement> driver, String element, int distance) {
        Point start = driver.findElementByXPath("//*[@resource-id='" + element + "']").getLocation();
        int startX = start.x;
        int startY = start.y;
        Dimension wh = driver.findElementByXPath("//*[@resource-id='" + element + "']").getSize();
        int Widthx = wh.getWidth();
        int Heighty = wh.getHeight();
        int endX = Widthx + startX;
        int endY = Heighty + startY;
        (new TouchAction(driver))
                .longPress(PointOption.point(startX, endY - Heighty / 2))
                .moveTo(PointOption.point(startX + Widthx / distance, endY))
                .release()
                .perform();
    }

    /**
     * press短按
     *
     * @param driver
     * @param x
     * @param y
     */
    public static void press(AndroidDriver<AndroidElement> driver, int x, int y) {
        WaitOptions waitOption = WaitOptions.waitOptions(Duration.ofSeconds(1));
        new TouchAction<>(driver).press(PointOption.point(x, y)).waitAction(waitOption).release().perform();
    }

//    https://blog.csdn.net/ouyanggengcheng/article/details/89184266

    /**
     * longPress 长按
     * 这是一个往上滑动的操作，保持x坐标的不变，y坐标减少，表示从下往上滑动。操作过程是长按--等待1S--滑动--释放--执行
     *
     * @param driver
     * @param x
     * @param y
     */
    public static void longPress(AndroidDriver<AndroidElement> driver, int x, int y) {
        WaitOptions waitOption = WaitOptions.waitOptions(Duration.ofSeconds(1));
        new TouchAction<>(driver).longPress(PointOption.point(x, y)).waitAction(waitOption).release().perform();
//        PointOption startPoint = PointOption.point(1200, 1200);
//        PointOption endPointPoint = PointOption.point(1200, 600);
//        new TouchAction<>(driver).longPress(startPoint).waitAction(waitOption).moveTo(endPointPoint).release().perform();

    }

    /**
     * 截屏并保存至本地
     *
     * @throws InterruptedException
     */
    public static void getScreen(AndroidDriver<AndroidElement> driver, String fileName) {
        File screenShot = driver.getScreenshotAs(OutputType.FILE);
        File localScreenShot = new File(AndroidCapabilityType.LOCAL_SCREEN_FILE_URL + "/" + fileName
                + AndroidCapabilityType.LOCAL_SCREEN_FILE_FORMAT);
        try {
            FileUtils.copyFile(screenShot, localScreenShot);
        } catch (IOException e) {
            log.info("文件名、目录名或卷标语法不正确。" + e);

        }
    }

    /**
     * 截图全屏
     *
     * @param path
     * @param fileName
     * @throws Exception
     */
    public static void takeScreen(AndroidDriver<AndroidElement> driver, String path, String fileName) throws Exception {
        File srcFile = driver.getScreenshotAs(OutputType.FILE);
        System.out.println(path + fileName);
        FileUtils.copyFile(srcFile, new File(path + "/" + fileName));
    }

    /**
     * 判断元素是否存在
     *
     * @param by By.id("xxxx") By.xpath("xxx")  NoSuchElementException
     * @return 存在则返回true，不存在则返回false
     */
    public static boolean isElementExist(AndroidDriver<AndroidElement> driver, By by) {
        try {
            driver.findElement(by).isDisplayed();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 在指定超时时间内元素是否存在，如存在则立即返回结果，如不存在则在超时后返回结果
     *
     * @param by      定位对象
     * @param timeout 超时时间
     * @return 指定时间内任意时间该元素出现则停止等待返回true，指定时间内没出现则返回false
     */
    public static boolean isElementExist(AndroidDriver<AndroidElement> driver, By by, int timeout) {
        try {
            new WebDriverWait(driver, timeout).until(ExpectedConditions.presenceOfElementLocated(by));
            return true;
        } catch (Exception e) {
            log.debug("element not found");
            return false;
        }
    }

    /**
     * 查找元素存在则返回该元素对象，不存在则返回null
     *
     * @param by
     * @return
     */
    public static AndroidElement findElement(AndroidDriver<AndroidElement> driver, By by) {
        try {
            AndroidElement element = driver.findElement(by);
            return element;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 在指定超时时间内查找元素是否存在，如存在则立即返回AndroidElement对象，如不存在则在超时后返回null
     *
     * @param by
     * @param timeout 单位是秒
     * @return
     */
    public static AndroidElement findElement(AndroidDriver<AndroidElement> driver, final By by, int timeout) {
        try {
            AndroidElement element = new WebDriverWait(driver, timeout).until(new ExpectedCondition<AndroidElement>() {
                @Override
                public AndroidElement apply(WebDriver driver) {
                    return (AndroidElement) driver.findElement(by);
                }
            });
            return element;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 通过坐标点击某个坐标点
     *
     * @param x
     * @param y
     */
    public static void clickByCoordinate(AndroidDriver<AndroidElement> driver, int x, int y) {
        try {
            TouchAction ta = new TouchAction(driver);
            ta.tap(PointOption.point(x, y)).release().perform();
            log.info("点击坐标" + x + "  " + y + "成功");
        } catch (Exception e) {
            e.printStackTrace();
            log.info("点击坐标" + x + "  " + y + "报错");
        }
    }

    /**
     * 根据时间长按
     *
     * @param time
     */
    public static void longPress(AndroidDriver<AndroidElement> driver, final Long time) {
        try {
            TouchAction ta = new TouchAction(driver);
            ta.longPress(LongPressOptions.longPressOptions().withDuration(Duration.ofSeconds(time))).release().perform();
            ta.longPress(LongPressOptions.longPressOptions()).release().perform();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * dos命令执行滑动
     *
     * @param uuid
     * @param num
     */
    public static void DosSwipeDown(String uuid, int num) {
        String commd = "adb -s " + uuid + " shell input touchscreen swipe 400 800 400 400";
        log.info("执行dos 命令" + commd);
        for (int i = 0; i < num; i++) {
            OperationalCmd.runCmdCommand(commd);
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
