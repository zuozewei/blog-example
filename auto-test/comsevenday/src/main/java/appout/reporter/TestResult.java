package appout.reporter;

import java.util.List;

/**
 * @author 7DGroup
 * @Title: TestResult
 * @Description: 用于存储测试结果
 * @date 2019/11/21 / 19:04
 */

public class TestResult {
    /**
     * 测试方法名
     */
    private String testName;
    /**
     * 测试类名
     */
    private String className;
    /**
     * 用例名称
     */
    private String caseName;
    /**
     * 测试用参数
     */
    private String params;
    /**
     * 测试描述
     */
    private String description;
    /**
     * 报告输出日志Reporter Output
     */
    private List<String> output;
    /**
     * 测试异常原因
     */
    private Throwable throwable;

    /**
     * 线程信息
     */
    private String throwableTrace;
    /**
     * 状态
     */
    private int status;
    /**
     * 持续时间
     */
    private String duration;
    /**
     * 是否成功
     */
    private boolean success;

    public TestResult() {
    }


    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getCaseName() {
        return caseName;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getOutput() {
        return output;
    }

    public void setOutput(List<String> output) {
        this.output = output;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public String getThrowableTrace() {
        return throwableTrace;
    }

    public void setThrowableTrace(String throwableTrace) {
        this.throwableTrace = throwableTrace;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "TestResult{" +
                "testName='" + testName + '\'' +
                ", className='" + className + '\'' +
                ", caseName='" + caseName + '\'' +
                ", params='" + params + '\'' +
                ", description='" + description + '\'' +
                ", output=" + output +
                ", throwable=" + throwable +
                ", throwableTrace='" + throwableTrace + '\'' +
                ", status=" + status +
                ", duration='" + duration + '\'' +
                ", success=" + success +
                '}';
    }
}
