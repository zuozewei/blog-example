package com.sevendays.utils;

import com.sevendays.service.impl.JmerterScriptServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author 7DGroup
 * @Title: execCmd
 * @Description: 直接执行命令
 * @date 2019/11/17 / 19:17
 */

public class execCmd {

    private static final Logger logger = LoggerFactory.getLogger(execCmd.class);

    public execCmd() {
    }


    /**
     * 直接执行命令
     *
     * @param cmd
     */
    public static void execCmd(String cmd) {
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(cmd, null, null);
            InputStream stderr = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(stderr, "GBK");
            BufferedReader br = new BufferedReader(isr);
            String line = "";
            while ((line = br.readLine()) != null) {
                logger.info(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 替换文本文件中的 非法字符串
     *
     * @param path   路径
     * @param srcStr 原有的内容
     * @param newStr 要替换的内容
     */
    public static void replacTextContent(String path, String srcStr, String newStr) {
        File file = new File(path);
        if (!file.exists() && !file.isFile()) {
            logger.info("file{},不存在:", path);
            return;
        }
        try {
            FileReader in = new FileReader(file);
            BufferedReader bufIn = new BufferedReader(in);
            // 内存流, 作为临时流
            CharArrayWriter tempStream = new CharArrayWriter();
            // 替换
            String line = null;
            while ((line = bufIn.readLine()) != null) {
                // 替换每行中, 符合条件的字符串
                line = line.replaceAll(srcStr, newStr);
                // 将该行写入内存
                tempStream.write(line);
                // 添加换行符
                tempStream.append(System.getProperty("line.separator"));
            }
            // 关闭 输入流
            bufIn.close();
            // 将内存中的流 写入 文件
            FileWriter out = new FileWriter(file);
            tempStream.writeTo(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("====path:" + path);

    }

}
