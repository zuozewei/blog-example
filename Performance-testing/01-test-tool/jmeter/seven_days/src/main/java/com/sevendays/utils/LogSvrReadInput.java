package com.sevendays.utils;

import com.sevendays.service.impl.JmerterScriptServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author 7d
 * @Title: LogSvrReadInput
 * @Description: 日志读取
 * @date 2019/11/17 / 21:10
 */
public class LogSvrReadInput {

    private static final Logger logger = LoggerFactory.getLogger(LogSvrReadInput.class);

    //上次文件大小
    private static long lastTimeFileSize = 0;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
    private static ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);


    /**
     * 写入日志
     *
     * @param logFile
     * @param msgInfo
     * @throws IOException
     */
    public static void logMsg(File logFile, String msgInfo) throws IOException {

        if (!logFile.exists()) {
            logFile.createNewFile();
        }

        Writer txtWriter = new FileWriter(logFile, true);
        txtWriter.write(dateFormat.format(new Date()) + "\t" + msgInfo + "\n");
        txtWriter.flush();
        txtWriter.close();
    }


    public void stop() {
        if (exec != null) {
            exec.shutdown();
            logger.info("file write stop ！");
        }
    }


    /**
     * 根据文件大小实时读取数据
     *
     * @param logFile
     * @throws Exception
     */
    public static String realtimeShowLog(File logFile) {
        final String[] line = {null};
        if (logFile == null) {
            throw new IllegalStateException("logFile can not be null");
        }
        //启动一个线程每2秒读取新增的日志信息
        exec.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                //获得变化部分
                try {
                    long len = logFile.length();
                    if (len < lastTimeFileSize) {
                        logger.info("Log file was reset. Restarting logging from start of file.");
                        lastTimeFileSize = 0;
                    } else {
                        //指定文件可读可写
                        RandomAccessFile randomFile = new RandomAccessFile(logFile, "rw");
                        //获取RandomAccessFile对象文件指针的位置，初始位置是0
                        logger.info("RandomAccessFile文件指针的初始位置:" + lastTimeFileSize);
                        //移动文件指针位置
                        randomFile.seek(lastTimeFileSize);

                        String tmp = "";
                        while ((tmp = randomFile.readLine()) != null) {
                            logger.info("info : " + new String(tmp.getBytes("utf-8")));
                            line[0] = ("info : " + new String(tmp.getBytes("utf-8")));
                        }
                        lastTimeFileSize = randomFile.length();
                        randomFile.close();
                    }

                } catch (Exception e) {
                    //实时读取日志异常，需要记录时间和lastTimeFileSize 以便后期手动补充
                    logger.error(dateFormat.format(new Date()) + " File read error, lastTimeFileSize: " + lastTimeFileSize);
                } finally {
                    //将lastTimeFileSize 落地以便下次启动的时候，直接从指定位置获取
                }
            }
        }, 0, 10, TimeUnit.SECONDS);

        return line[0];
    }
}
