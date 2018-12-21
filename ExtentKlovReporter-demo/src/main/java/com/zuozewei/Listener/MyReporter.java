package com.zuozewei.Listener;

import com.aventstack.extentreports.ExtentTest;

/**
 * @Auther: zuozewei
 * @Date: 2018/12/15 17:18
 * @Description:
 */
public class MyReporter {
    public static ExtentTest report;
    private static String testName;

    public static String getTestName() {
        return testName;
    }

    public static void setTestName(String testName) {
        MyReporter.testName = testName;
    }
}
