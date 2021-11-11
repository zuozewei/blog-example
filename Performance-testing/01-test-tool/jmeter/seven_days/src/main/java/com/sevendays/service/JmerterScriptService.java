package com.sevendays.service;

/**
 * @author 7DGroup
 * @Title: JmerterScriptService
 * @Description: Jmeterj脚本处理
 * @date 2019/11/17 / 18:06
 */

public interface JmerterScriptService {

    /**
     * 执行命令
     * @param cmd
     */
    void execCommand(String cmd);

    /**
     * 运行
     * @param script 脚本
     * @param num  数量
     * @param seconds 执行时间
      */
    void runCommand(String script, String num,String seconds);

    /**
     * 停止
     */
    void stopCommand();

    /**
     * 获取日志
     * @return
     */
    String selectInfo();

}
