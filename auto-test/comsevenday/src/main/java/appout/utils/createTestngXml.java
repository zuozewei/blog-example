package appout.utils;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * @author 7DGroup
 * @Title: createTestngXml
 * @Description: 生存xml文件
 * @date 2019/11/24 / 16:15
 */

public class createTestngXml {

    static Properties sysProperty = System.getProperties();

    public static void createTestngXml(String classname) {
        Document document = DocumentHelper.createDocument();
        Element root = DocumentHelper.createElement("suite");
        document.setRootElement(root);
        root.addAttribute("name", "automation");
//        parallel：由TestNG 运行不同的线程来运行此套件。
        root.addAttribute("parallel", "Automatically generate xml");
        root.addAttribute("thread-count", String.valueOf("1"));
        //生成监听器
        Element listeners = root.addElement("listeners");
        Element listener1 = listeners.addElement("listener");
        listener1.addAttribute("class-name", "appout.reporter.ReporterListener");
        Element test = root.addElement("test");

        //增加设备号与端口号：
        Element paramUuid = test.addElement("parameter");
        paramUuid.addAttribute("name", "udid");
        //根据实际情况修改
        paramUuid.addAttribute("value", "127.0.0.1:62001");
        Element paramPort = test.addElement("parameter");
        paramPort.addAttribute("name", "port");
        //根据实际情况修改
        paramPort.addAttribute("value", "4723");

        test.addAttribute("name", "127.0.0.1");
        Element classes = test.addElement("classes");
        Element classNode = classes.addElement("class");
        classNode.addAttribute("name", classname);
        OutputFormat format = new OutputFormat("    ", true);
        XMLWriter xmlWrite2;
        try {
            xmlWrite2 = new XMLWriter(new FileOutputStream(sysProperty.getProperty("user.dir") +"/comsevenday/src/main/resources/appxml/mytestng.xml"), format);
            xmlWrite2.write(document);
        } catch (
                UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void ee() {
        createTestngXml("appout.appcase.CartTest");
    }


}
