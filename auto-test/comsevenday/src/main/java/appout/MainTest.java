package appout;

import appout.utils.OperationalCmd;
import appout.utils.createTestngXml;
import org.testng.TestNG;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author 7DGroup
 * @Title: MainTest
 * @Description: 主方法
 * @date 2019/11/24 / 19:49
 */

public class MainTest {

    static Properties sysProperty = System.getProperties();
    private static final String ESCAPE_PROPERTY = "org.uncommons.reportng.escape-output";

    public static void main(String[] args) {

        //先执行kill
        OperationalCmd.killServer();
        //在启动服务
        OperationalCmd.startAppium();

        try {
            //执行的类
            createTestngXml.createTestngXml("appout.appcase.CartTest");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.setProperty(ESCAPE_PROPERTY, "false");
        List<String> suites = new ArrayList<String>();
        System.out.println(suites);
        suites.add(sysProperty.getProperty("user.dir") + "/comsevenday/src/main/resources/appxml/mytestng.xml");
        TestNG tng = new TestNG();
        tng.setTestSuites(suites);
        tng.run();
    }
}
