package com.zuozewei.demo;

import org.apache.jmeter.JMeter;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 上传现成脚本demo
 * 1）加载已有JMX文件并解析
 * 2）生成.csv格式结果
 * * */

public class JMeterDemo2 {

    public static void main(String[] argv) throws Exception {
    	// 设置jmeterHome路径
		String jmeterHome1 = "/Users/apple/Downloads/performance/apache-jmeter-4.0";
        //File jmeterHome = new File(System.getProperty("jmeter.home"));
		File jmeterHome = new File(jmeterHome1);
		File jmxFile = new File(jmeterHome1 + "/example.jmx");

		// 分隔符
        String slash = System.getProperty("file.separator");

        // 判断jmeterHome
        if (jmeterHome.exists()) {
            File jmeterProperties = new File(jmeterHome.getPath() + slash + "bin" + slash + "jmeter.properties");
            if (jmeterProperties.exists()) {

            	// 初始化压测引擎
                StandardJMeterEngine jmeter = new StandardJMeterEngine();

                // JMeter初始化(属性、日志级别、区域设置等)
                JMeterUtils.setJMeterHome(jmeterHome.getPath());
                JMeterUtils.loadJMeterProperties(jmeterProperties.getPath());
                // 可以注释这一行，查看额外的日志，例如DEBUG级别
                JMeterUtils.initLogging();
                JMeterUtils.initLocale();


                // JMeter测试计划，基本上是JOrphan HashTree
                HashTree testPlanTree = new HashTree();

				// 设置jmx脚本文件的工作目录，可以根据这个来找到参数化文件及实现其文件流。
				FileServer.getFileServer().setBaseForScript(jmxFile);

				// 加载jmx脚本，本身这个操作非常复杂。
				// jmx脚本中通常会包含参数化文件，用户自定义的参数化，Jmeter自定义函数，各种Sampler的实现，断言，甚至用户自定义的插件等等。
				// 同时还有各种监听接口的初始化。
				// 这些都是要找到实现类加载的，源码中包含非常多的实现类。
				testPlanTree = SaveService.loadTree(jmxFile);

				// 这个是递归实现的，比较复杂
				JMeter.convertSubTree(testPlanTree);


				// 在stdout中添加summary输出，得到测试进度，如:
                // summary =      2 in   1.3s =    1.5/s Avg:   631 Min:   290 Max:   973 Err:     0 (0.00%)
                Summariser summer = null;
                String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
                if (summariserName.length() > 0) {
                    summer = new Summariser(summariserName);
                }


				// 将执行结果存储到.csv文件中
                String logFile = jmeterHome + slash + "example.csv";
                ResultCollector logger = new ResultCollector(summer);
                logger.setFilename(logFile);
                testPlanTree.add(testPlanTree.getArray()[0], logger);


				// 单机执行测试计划
                jmeter.configure(testPlanTree);  // 设置回调监听器，并添加状态
                jmeter.run();

                System.out.println("生成结果文件：" + jmeterHome + slash + "example.csv");
                System.out.println("加载Jmx脚本文件：" + jmeterHome + slash + "example.jmx");
                System.exit(0);

            }
        }

        System.err.println("jmeter.home 未设置或指向不正确的位置");
        System.exit(1);


    }
}
