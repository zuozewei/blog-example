package com.zuozewei.demo;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.JMeterEngine;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 代码生成测试脚本，及JMX文件Demo
 * 1）先定义每个组件的生成方式，然后再按一定结构组装各个组件，最后生成JMX文件
 * 2）生成.jtl结果文件
 * 2）单机压测
 */

public class JMeterDemo1 {

    public static void main(String[] argv) throws Exception {

    	// 设置jmeterHome路径
		// 主要是读取了几个配置文件，jmeter.properties，user.properties，system.properties。
		// 设置一下的本地的Locale环境。
		// 其实到这里，是可以仅将这3个配置文件抽离出来，即不需要整个Jmeter的home目录，仅要这3个配置文件就能运行Jmeter脚本。
		// 甚至仅在代码中写要的配置，都不需要实体的配置文件即可。
		// 当然随着功能越来越多，平台跟Jmeter的耦合也越来越多，这个Jmeter_home目录还是越来越必要了。
		String jmeterHome1 = "/Users/apple/Downloads/performance/apache-jmeter-4.0";
        //File jmeterHome = new File(System.getProperty("jmeter.home"));
		File jmeterHome = new File(jmeterHome1);
		// 分隔符
        String slash = System.getProperty("file.separator");

        //判断jmeterHome
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

                // 第一个 HTTP Sampler - 打开 baidu.com
                HTTPSamplerProxy baiducomSampler = new HTTPSamplerProxy();
				baiducomSampler.setDomain("baidu.com");
				baiducomSampler.setPort(80);
				baiducomSampler.setPath("/");
				baiducomSampler.setMethod("GET");
				baiducomSampler.setName("Open baidu.com");
				baiducomSampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
				baiducomSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());


                // 第二个 HTTP Sampler - 打开 qq.com
                HTTPSamplerProxy qqcomSampler = new HTTPSamplerProxy();
				qqcomSampler.setDomain("qq.com");
				qqcomSampler.setPort(80);
				qqcomSampler.setPath("/");
				qqcomSampler.setMethod("GET");
				qqcomSampler.setName("Open qq.com");
				qqcomSampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
				qqcomSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());


                // Loop Controller 循环控制
                LoopController loopController = new LoopController();
                loopController.setLoops(1);
                loopController.setFirst(true);
                loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
                loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
                loopController.initialize();

                // Thread Group 线程组
                ThreadGroup threadGroup = new ThreadGroup();
                threadGroup.setName("Example Thread Group");
                threadGroup.setNumThreads(1);
                threadGroup.setRampUp(1);
                threadGroup.setSamplerController(loopController);
                threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
                threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());

                // Test Plan 测试计划
                TestPlan testPlan = new TestPlan("创建JMeter脚本");
                testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
                testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
                testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());

				// 从以上初始化的元素构造测试计划
                testPlanTree.add(testPlan);
                HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
                threadGroupHashTree.add(baiducomSampler);
                threadGroupHashTree.add(qqcomSampler);

				// 将生成的测试计划保存为JMeter的.jmx文件格式
                SaveService.saveTree(testPlanTree, new FileOutputStream(jmeterHome + slash + "example.jmx"));

				// 在stdout中添加summary输出，得到测试进度，如:
                // summary =      2 in   1.3s =    1.5/s Avg:   631 Min:   290 Max:   973 Err:     0 (0.00%)
                Summariser summer = null;
                String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
                if (summariserName.length() > 0) {
                    summer = new Summariser(summariserName);
                }


				// 将执行结果存储到.jtl文件中
                String logFile = jmeterHome + slash + "example.jtl";
                ResultCollector logger = new ResultCollector(summer);
                logger.setFilename(logFile);
                testPlanTree.add(testPlanTree.getArray()[0], logger);


				// 执行测试计划
                jmeter.configure(testPlanTree);  // 设置回调监听器，并添加状态
                jmeter.run();

                System.out.println("生成结果文件：" + jmeterHome + slash + "example.jtl");
                System.out.println("Jmx脚本文件：" + jmeterHome + slash + "example.jmx");
                System.exit(0);

            }
        }

        System.err.println("jmeter.home 未设置或指向不正确的位置");
        System.exit(1);


    }
}
