package appout.appcase.data;

import appout.utils.GetByLocator;
import appout.utils.LogUtil;
import appout.utils.WaitUtil;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import java.util.List;

/**
 * @author 7DGroup
 * @Title: CartProvider
 * @Description: this is
 * @date 2019/11/23 / 10:57
 */

public class CartProvider {
    private GetByLocator getByLocator;
    private WebElement element = null;

    /**
     * 构造函数
     */
    public CartProvider() {
        this.getByLocator = new GetByLocator("loginElement.properties");
    }

    /**
     * 点击首页
     */
    public void clickHome(AndroidDriver<AndroidElement> driver) {
        WaitUtil.waitWebElement(driver, getByLocator.getLocatorApp("clickCart"), "点击首页");
        element = driver.findElement(getByLocator.getLocatorApp("clickCart"));
        element.click();
    }

    /**
     * 点击百宝箱
     *
     * @param driver
     */
    public void clickbaibao(AndroidDriver<AndroidElement> driver) {
        WaitUtil.waitWebElement(driver, getByLocator.getLocatorApp("clickCart"), "百宝箱");
        String[] name = {"京东超市", "数码电器", "京东服饰", "京东生鲜"};
        for (int i = 0; i < name.length; i++) {
            element = driver.findElement(getByLocator.getLocatorApp("Jingdong_supermarket".replace("京东超市", name[i])));
            element.click();
            //断言
            assertTrue(driver, "assertTitile", name[i] + "跳转成功");
            //退回
            WaitUtil.waitWebElement(driver, getByLocator.getLocatorApp("home_back"), "退回");
            driver.findElement(getByLocator.getLocatorApp("home_back"));

        }
    }


    /**
     * 分类写法
     *
     * @param driver
     */
    public void categorylist(AndroidDriver<AndroidElement> driver) {
        WaitUtil.waitWebElement(driver, getByLocator.getLocatorApp("categorylist"), "遍历分类");
        //获取全部数据挨个遍历
        List<AndroidElement> categorylist = driver.findElements(getByLocator.getLocatorApp("categorylist"));
        for (int i = 0; i < categorylist.size(); i++) {
            String text = categorylist.get(i).getText();
            //输出遍历name
            LogUtil.info(text);
            categorylist.get(i).click();
        }

    }

    /**
     * 收藏
     *
     * @param driver
     */
    public void productdetail(AndroidDriver<AndroidElement> driver) {
        WaitUtil.waitWebElement(driver, getByLocator.getLocatorApp("productdetail"), "收藏");
        AndroidElement detail = driver.findElement(getByLocator.getLocatorApp("productdetail"));
        String attribute = detail.getAttribute("selected");
        if (attribute.equals(true)) {
            LogUtil.info("目前商品收藏，目前的属性为：" + attribute);
        } else {
            LogUtil.info("商品没有收藏，目前的属性为：" + attribute);
        }
    }


    /**
     * 统一判断是否通过
     *
     * @param driver 传driver
     * @param elemn  定元素
     * @param des    描述
     */
    public void assertTrue(AndroidDriver<AndroidElement> driver, String elemn, String des) {
        WaitUtil.waitWebElement(driver, getByLocator.getLocatorApp(elemn), des);
        element = driver.findElement(getByLocator.getLocatorApp(elemn));
        String text = element.getText();
        if (text != null) {
            Assert.assertTrue(true, des + "验证成功");
        } else {
            //如果失败系统会自动截图
            Assert.fail("跳转失败");
        }
    }
}


//     Assert.assertTrue("", "验证跳转成功");
//            driver.findElement(By.id(""))

