package appout.utils;

import org.apache.log4j.Logger;

/**
 * @author 7DGroup
 * @Title: LogUtil
 * @Description: 日志类
 * @date 2019/11/21 / 19:11
 */

public class LogUtil {
    private LogUtil() {
    }

    private static Logger Log = Logger.getLogger(LogUtil.class);

    public static void startTestCase(String sTestCaseName) {

        Log.info("----------   " + "测试用例开始执行" + "    ----------------");
        Log.info("----------   " + sTestCaseName + "    ----------------");
    }

    public static void endTestCase(String sTestCaseName) {
        Log.info("----------   " + "测试用例执行结束" + "    ----------------");
        Log.info("----------   " + sTestCaseName + "    ----------------");
    }

    public static void info(String message) {
        Log.info(message);
    }

    public static void warn(String message) {
        Log.warn(message);
    }

    public static void error(String message) {
        Log.error(message);
    }

    public static void fatal(String message) {
        Log.fatal(message);
    }

    public static void debug(String message) {
        Log.debug(message);
    }
}
