package com.techstar.plugins.configelement.PropertyReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author zuozewei
 */
public class PropertyReader extends ConfigTestElement implements TestBean, TestStateListener {

    //定义根日志器
    private static final Logger logger = LogManager.getLogger(PropertyReader.class);
    //配置文件路径
    private String propFilePath;

    public PropertyReader(){
        super();
    }

    /**
     * 在主线程测试开始之前被调用
     * 我们需要在测试前加载配置
     */
    public void testStarted() {
        //判断字段是否为空
        if (StringUtils.isNotEmpty(getPropFilePath())){
            try {
                //使用传入的字符串返回一个Path对象
                Path path = Paths.get(getPropFilePath());
                //判断是否为为绝对路径
                if (!path.isAbsolute()) {
                    //得到文件路径
                    path = Paths.get(FileServer.getFileServer().getBaseDir(), path.toString());
                }
                //加载配置文件
                JMeterUtils.getJMeterProperties().load(new FileInputStream(path.toString()));
                logger.info("loading Property："+path);
                } catch (FileNotFoundException e) {
                    logger.error(e.getMessage());
                } catch (IOException e){
                    logger.error(e.getMessage());
            }
            }
        }

    /**
     * 在主线程测试开始之前被调用
     * @param s
     */
    public void testStarted(String s) {
        testStarted();
    }

    /**
     * 测试结束后，所有线程都调用一次
     */
    public void testEnded() { }

    /**
     * 测试结束后，所有线程都调用一次
     * @param s
     */
    public void testEnded(String s) { }

    /**
     * 返回配置文件路径
     * @return
     */
    public String getPropFilePath(){
        return this.propFilePath;
    }

    /**
     * 读取配置文件路径
     * @param propFilePath
     */
    public void setPropFilePath(String propFilePath){
        this.propFilePath = propFilePath;
    }
}
