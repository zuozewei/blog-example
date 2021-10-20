package appout.reporter;


import appout.base.DriverBase;
import appout.utils.LogUtil;
import appout.utils.OperationalCmd;
import io.appium.java_client.AppiumDriver;
import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.openqa.selenium.OutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.*;
import org.testng.xml.XmlSuite;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author 7DGroup
 * @Title: ReporterListener
 * @Description: 自定义报告监听类
 * @date 2019/11/21 / 18:56
 */

public class ReporterListener implements IReporter, ITestListener {
    private static final Logger log = LoggerFactory.getLogger(DriverBase.class);
    private static final NumberFormat DURATION_FORMAT = new DecimalFormat("#0.000");

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        List<ITestResult> list = new LinkedList<>();
        Date startDate = new Date();
        Date endDate = new Date();

        int TOTAL = 0;
        int SUCCESS = 1;
        int FAILED = 0;
        int ERROR = 0;
        int SKIPPED = 0;
        for (ISuite suite : suites) {
            Map<String, ISuiteResult> suiteResults = suite.getResults();
            for (ISuiteResult suiteResult : suiteResults.values()) {
                ITestContext testContext = suiteResult.getTestContext();

                startDate = startDate.getTime() > testContext.getStartDate().getTime() ? testContext.getStartDate() : startDate;

                if (endDate == null) {
                    endDate = testContext.getEndDate();
                } else {
                    endDate = endDate.getTime() < testContext.getEndDate().getTime() ? testContext.getEndDate() : endDate;
                }

                IResultMap passedTests = testContext.getPassedTests();
                IResultMap failedTests = testContext.getFailedTests();
                IResultMap skippedTests = testContext.getSkippedTests();
                IResultMap failedConfig = testContext.getFailedConfigurations();

                SUCCESS += passedTests.size();
                FAILED += failedTests.size();
                SKIPPED += skippedTests.size();
                ERROR += failedConfig.size();

                list.addAll(this.listTestResult(passedTests));
                list.addAll(this.listTestResult(failedTests));
                list.addAll(this.listTestResult(skippedTests));
                list.addAll(this.listTestResult(failedConfig));
            }
        }
        /* 计算总数 */
        TOTAL = SUCCESS + FAILED + SKIPPED + ERROR;

        this.sort(list);
        Map<String, TestResultCollection> collections = this.parse(list);
        VelocityContext context = new VelocityContext();

        context.put("TOTAL", TOTAL);
        context.put("mobileModel", OperationalCmd.getMobileModel());
        context.put("versionName", OperationalCmd.getVersionNameInfo());
        context.put("SUCCESS", SUCCESS);
        context.put("FAILED", FAILED);
        context.put("ERROR", ERROR);
        context.put("SKIPPED", SKIPPED);
        context.put("startTime", ReporterListener.formatDate(startDate.getTime()) + "<--->" + ReporterListener.formatDate(endDate.getTime()));
        context.put("DURATION", ReporterListener.formatDuration(endDate.getTime() - startDate.getTime()));
        context.put("results", collections);
        write(context, outputDirectory);
    }

    /**
     * 输出模板
     *
     * @param context
     * @param outputDirectory
     */
    private void write(VelocityContext context, String outputDirectory) {
        if (!new File(outputDirectory).exists()) {
            new File(outputDirectory).mkdirs();
        }
        //获取报告模板
        File f = new File("");
        String absolutePath = f.getAbsolutePath();
        String fileDir = absolutePath + "/template/";
        String reslutpath = outputDirectory + "/html/report" + ReporterListener.formateDate() + ".html";
        File outfile = new File(reslutpath);
        if (!outfile.exists()) {
            outfile.mkdirs();
        }
        try {
            //写文件
            VelocityEngine ve = new VelocityEngine();
            Properties p = new Properties();
            p.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, fileDir);
            p.setProperty(Velocity.ENCODING_DEFAULT, "utf-8");
            p.setProperty(Velocity.INPUT_ENCODING, "utf-8");
            ve.init(p);

            Template t = ve.getTemplate("reportnew.vm");
            //输出结果
            OutputStream out = new FileOutputStream(new File(reslutpath));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
            // 转换输出
            t.merge(context, writer);
            writer.flush();
            log.info("报告位置：" + reslutpath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 排序规则
     *
     * @param list
     */
    private void sort(List<ITestResult> list) {
        Collections.sort(list, new Comparator<ITestResult>() {
            @Override
            public int compare(ITestResult r1, ITestResult r2) {
                if (r1.getStatus() < r2.getStatus()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
    }

    private LinkedList<ITestResult> listTestResult(IResultMap resultMap) {
        Set<ITestResult> results = resultMap.getAllResults();
        return new LinkedList<>(results);
    }

    private Map<String, TestResultCollection> parse(List<ITestResult> list) {

        Map<String, TestResultCollection> collectionMap = new HashMap<>();

        for (ITestResult t : list) {
            String className = t.getTestClass().getName();
            if (collectionMap.containsKey(className)) {
                TestResultCollection collection = collectionMap.get(className);
                collection.addTestResult(toTestResult(t));

            } else {
                TestResultCollection collection = new TestResultCollection();
                collection.addTestResult(toTestResult(t));
                collectionMap.put(className, collection);
            }
        }

        return collectionMap;
    }

    /**
     * 输出报表解析
     * @param t
     * @return
     */
    private appout.reporter.TestResult toTestResult(ITestResult t) {
        TestResult testResult = new TestResult();
        Object[] params = t.getParameters();

        if (params != null && params.length >= 1) {
            String caseId = (String) params[0];
            testResult.setCaseName(caseId);
        } else {
            testResult.setCaseName("null");
        }
        testResult.setClassName(t.getTestClass().getName());
        testResult.setParams(getParams(t));
        testResult.setTestName(t.getName());
        testResult.setDescription(t.getMethod().getDescription());
        testResult.setStatus(t.getStatus());
        //异常
        testResult.setThrowableTrace("class: " + t.getTestClass().getName() + " <br/> method: " + t.getName() + " <br/> error: " + t.getThrowable());
        testResult.setThrowable(t.getThrowable());
        long duration = t.getEndMillis() - t.getStartMillis();
        testResult.setDuration(formatDuration(duration));
        //日志
        testResult.setOutput(Reporter.getOutput(t));
        return testResult;
    }

    /**
     * 每次调用测试@Test之前调用
     *
     * @param result
     */
    @Override
    public void onTestStart(ITestResult result) {
        logTestStart(result);

    }

    /**
     * 用例执行结束后，用例执行成功时调用
     *
     * @param result
     */
    @Override
    public void onTestSuccess(ITestResult result) {
        logTestEnd(result, "Success");
    }

    /**
     * 用例执行结束后，用例执行失败时调用
     * 跑fail则截图 获取屏幕截图
     *
     * @param result
     */

    @Override
    public void onTestFailure(ITestResult result) {

        AppiumDriver driver = DriverBase.getDriver();
        File srcFile = driver.getScreenshotAs(OutputType.FILE);

        File location = new File("./test-output/html/result/screenshots");
        if (!location.exists()) {
            location.mkdirs();
        }
        String dest = result.getMethod().getRealClass().getSimpleName() + "." + result.getMethod().getMethodName();
        String s = dest + "_" + formateDate() + ".png";
        File targetFile =
                new File(location + "/" + s);
        log.info("截图位置：");
        Reporter.log("<font color=\"#FF0000\">截图位置</font><br /> " + targetFile.getPath());
        log.info("------file is ---- " + targetFile.getPath());
        try {
            FileUtils.copyFile(srcFile, targetFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logTestEnd(result, "Failed");
        //报告截图后面显示
        Reporter.log("<img  src=\"./result/screenshots/" + s + "\" width=\"64\" height=\"64\" alt=\"***\"  onMouseover=\"this.width=353; this.height=613\" onMouseout=\"this.width=64;this.height=64\" />");
    }

    /**
     * 用例执行结束后，用例执行skip时调用
     *
     * @param result
     */
    @Override
    public void onTestSkipped(ITestResult result) {
        logTestEnd(result, "Skipped");
    }

    /**
     * 每次方法失败但是已经使用successPercentage进行注释时调用，并且此失败仍保留在请求的成功百分比之内。
     *
     * @param result
     */
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        LogUtil.fatal(result.getTestName());
        logTestEnd(result, "FailedButWithinSuccessPercentage");
    }

    /**
     * 在测试类被实例化之后调用，并在调用任何配置方法之前调用。
     *
     * @param context
     */
    @Override
    public void onStart(ITestContext context) {
        LogUtil.startTestCase(context.getName());
        return;
    }

    /**
     * 在所有测试运行之后调用，并且所有的配置方法都被调用
     *
     * @param context
     */
    @Override
    public void onFinish(ITestContext context) {
        LogUtil.endTestCase(context.getName());
        return;
    }

    /**
     * 在用例执行结束时，打印用例的执行结果信息
     */
    protected void logTestEnd(ITestResult tr, String result) {
        Reporter.log(String.format("=============Result: %s=============", result), true);

    }

    /**
     * 在用例开始时，打印用例的一些信息，比如@Test对应的方法名，用例的描述等等
     */
    protected void logTestStart(ITestResult tr) {
        Reporter.log(String.format("=============Run: %s===============", tr.getMethod().getMethodName()), true);
        Reporter.log(String.format("用例描述: %s, 优先级: %s", tr.getMethod().getDescription(), tr.getMethod().getPriority()),
                true);
        return;
    }

    /**
     * 日期格式化
     *
     * @return date
     */
    public static String formateDate() {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        return sf.format(date);
    }

    /**
     * 时间转换
     *
     * @param date
     * @return
     */
    public static String formatDate(long date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }

    public static String formatDuration(long elapsed) {
        double seconds = (double) elapsed / 1000;
        return DURATION_FORMAT.format(seconds);
    }

    /**
     * 获取方法参数，以逗号分隔
     *
     * @param result
     * @return
     */
    public static String getParams(ITestResult result) {
        Object[] params = result.getParameters();
        List<String> list = new ArrayList<String>(params.length);
        for (Object o : params) {
            list.add(renderArgument(o));
        }
        return commaSeparate(list);
    }

    /**
     * 将object 转换为String
     * @param argument
     * @return
     */
    private static String renderArgument(Object argument) {
        if (argument == null) {
            return "null";
        } else if (argument instanceof String) {
            return "\"" + argument + "\"";
        } else if (argument instanceof Character) {
            return "\'" + argument + "\'";
        } else {
            return argument.toString();
        }
    }


    /**
     * 将集合转换为以逗号分隔的字符串
     * @param strings
     * @return
     */
    private static String commaSeparate(Collection<String> strings) {
        StringBuilder buffer = new StringBuilder();
        Iterator<String> iterator = strings.iterator();
        while (iterator.hasNext()) {
            String string = iterator.next();
            buffer.append(string);
            if (iterator.hasNext()) {
                buffer.append(", ");
            }
        }
        return buffer.toString();
    }

}
