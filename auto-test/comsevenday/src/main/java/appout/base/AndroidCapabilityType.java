package appout.base;

import java.io.File;

/**
 * @author 7DGroup
 * @Title: AndroidCapabilityType
 * @Description:功能配置类
 * @date 2019/6/26 / 15:01
 */

public class AndroidCapabilityType {


    private AndroidCapabilityType() {
    }

    public static final boolean NO_SIGN = true;
    public static final boolean UNICODE_KEY_BOARD = true;
    public static final boolean RESET_KEY_BOARD = true;
    /**
     * waitElement 时的最长等待时间
     */
    public static final String NEW_COMMAND_TIMEOUT = "600";
    public static final String PLATFORM_NAME = "Appium";
    public static final boolean FULL_RESET = true;
    /**
     * 向上小滑动一步
     */
    public static final String APP_UP_SWIPE = "adb shell input touchscreen swipe 400 800 400 300";
    public static final String APP_GET_PACK_ACTIVITY = "aapt d badging pathapk |findstr \"package launchable-activity\"";

    /**
     * 重启应用
     */
    public static final String RESTAPK = "adb -s 127.0.0.1 shell am start -n pathapk";


    /**adb*/

    /**
     * 根据包名得到进程
     */
    public static final String GETAPPPACKAGEPID = "adb shell ps | grep ";
    /**
     * 打开指定app
     */
    public static final String OPEN_APP = "shell am start -n packagename activity";
    /**
     * 本地存储截屏图片的目录，（注意配置时的格式）
     */

    public static final String LOCAL_SCREEN_FILE_URL = getpathlocal();

    /**
     * 获取目录工程路径
     *
     * @return
     */
    public static String getpathlocal() {
        File f = new File("");
        String logpath = f.getAbsolutePath() + "/test-output/html/screenshots";
        File file = new File(logpath);
        if (!file.exists()) {
            f.mkdirs();
        }
        return file.toString();

    }


    /**
     * 本地存储截屏图片的格式
     */
    public static final String LOCAL_SCREEN_FILE_FORMAT = ".png";
}
