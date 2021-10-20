package appout.utils;

import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author 7DGroup
 * @Title: GetByLocator
 * @Description: this is
 * @date 2019/11/23 / 10:51
 */

public class GetByLocator {

    Logger logger = LoggerFactory.getLogger(GetByLocator.class);

    private Properties properties = null;
    static Properties sysProperty = System.getProperties();

    public GetByLocator() {
    }

    public GetByLocator(String filename) {
        properties = new Properties();
        try {
            FileInputStream in = new FileInputStream((sysProperty.getProperty("user.dir") + "/comsevenday/src/main/resources/uiElement/" + filename));
            this.properties.load(in);
            in.close();
        } catch (Exception e) {
            logger.info("找不到文件：" + filename);
            e.printStackTrace();
        }
    }

    /**
     * 根据资源文件下文件获取k -v
     *
     * @param uielement
     * @param filename
     */
    public GetByLocator(String uielement, String filename) {
        InputStream in = this.getClass().getResourceAsStream("/" + uielement + "/" + filename);
        properties = new Properties();
        try {
            this.properties.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * pc 元素转换
     *
     * @return
     * @param: name
     */
    public By getLocateorElementPc(String name) {
        String locator = properties.getProperty(name);
        String locatortype = locator.split(">>")[0].toLowerCase();
        String locatorvalue = locator.split(">>")[1];
        logger.info("获取元素key: " + name + "\t 获取的定位类型：" + locatortype + "\t 获取的定位表达式: " + locatorvalue);
        if ("cssselector".equals(locatortype)) {
            return By.cssSelector(locatorvalue);
        } else if ("id".equals(locatortype)) {
            return By.id(locatorvalue);
        } else if ("name".equals(locatortype)) {
            return By.name(locatorvalue);
        } else if ("classname".equals(locatortype)) {
            return By.className(locatorvalue);
        } else if ("tagname".equals(locatortype)) {
            return By.tagName(locatorvalue);
        } else if ("linktext".equals(locatortype)) {
            return By.linkText(locatorvalue);
        } else if ("parialllinktest".equals(locatortype)) {
            return By.partialLinkText(locatorvalue);
        } else if ("xpath".equals(locatortype)) {
            return By.xpath(locatorvalue);
        } else {
            try {
                throw new Exception("输入的 locator type 未在程序中被定义：" + locatorvalue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 移动端元素定位
     *
     * @return
     * @param: name
     */
    public By getLocatorApp(String name) {
        String locator = properties.getProperty(name.trim());
        String locatorType = locator.split(">>")[0].toLowerCase();
        String locatorValue = locator.split(">>")[1];
        if (locatorType != null && locatorType != null) {
            logger.info("获取元素key: " + name + "\t 获取的定位类型：" + locatorType + "\t 获取的定位表达式: " + locatorValue);
            // 根据 locatorType 的变量值内容判断，返回何种定位方式的 By 对象
            return getkeyVule(locatorType, locatorValue);
        } else {
            new RuntimeException("元素没有获取到！");
            return null;
        }

    }



    /**
     * 读取元素
     * @param locatorType
     * @param locatorValue
     * @return By
     */
    public By getkeyVule(String locatorType, String locatorValue) {
        if ("id".equals(locatorType.toLowerCase())) {
            return By.id(locatorValue);
        } else if ("name".equals(locatorType.toLowerCase())) {
            return By.name(locatorValue);
        } else if (("classname".equals(locatorType.toLowerCase())) || ("class".equals(locatorType.toLowerCase()))) {
            return By.className(locatorValue);
        } else if (("tagname".equals(locatorType.toLowerCase())) || ("tag".equals(locatorType.toLowerCase()))) {
            return By.className(locatorValue);
        } else if (("linktext".equals(locatorType.toLowerCase())) || ("link".equals(locatorType.toLowerCase()))) {
            return By.linkText(locatorValue);
        } else if ("partiallinktext".equals(locatorType.toLowerCase())) {
            return By.partialLinkText(locatorValue);
        } else if (("cssselector".equals(locatorType.toLowerCase())) || ("css".equals(locatorType.toLowerCase()))) {
            return By.cssSelector(locatorValue);
        } else if ("xpath".equals(locatorType.toLowerCase())) {
            return By.xpath(locatorValue);
        } else {
            return By.tagName(locatorValue);
        }
    }


    @Test
    public void ss() throws IOException {
        InputStream in = this.getClass().getResourceAsStream("/uielement/loginElement.properties");
        Properties properties = new Properties();
        properties.load(in);
        String to_shop_text1 = properties.getProperty("clickCart");
        System.out.println(to_shop_text1);
    }
}
