package com.sevendays.service.impl;

import com.sevendays.controller.JmeterController;
import com.sevendays.service.JmerterScriptService;
import com.sevendays.utils.LogSvrReadInput;
import com.sevendays.utils.execCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * @author 7d
 * @Title: JmerterScriptServiceImpl
 * @Description: 执行命令
 * @date 2019/11/17 / 18:49
 */

@Service
public class JmerterScriptServiceImpl implements JmerterScriptService {

    private static final Logger logger = LoggerFactory.getLogger(JmerterScriptServiceImpl.class);


    @Override
    public void execCommand(String cmd) {
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

    @Override
    public void runCommand(String script, String num, String seconds) {

        //执行次数
        String numThread = "<stringProp name=\"ThreadGroup.num_threads\">#numThread</stringProp>";
        //执行时间
        String time = "<stringProp name=\"ThreadGroup.duration\">#timeDuration</stringProp>";
        String bak = "cp /home/7d/" + script + ".jmx /home/7d/" + script + "bak.jmx";
        String old = "/home/7d/" + script + ".jmx";
        execCmd.execCmd(bak);
        logger.info("路径：{}", old);
        //替换执行数量
        execCmd.replacTextContent(old, "#numThread", num);
        //替换执行时间
        execCmd.replacTextContent(old, "#timeDuration", seconds);
        String runcmd = "nohup jmeter -n -t /home/7d/#scriptName.jmx -l /home/7d/#scriptName.jtl -j /home/7d/jmeter.log > /home/7d/jmeterlog.log&".replaceAll("#scriptName", script);
        logger.info("运行命令{}", runcmd);
        execCmd.execCmd(runcmd);
    }

    @Override
    public void stopCommand() {
        String stoprunm = "/root/tools/apache-jmeter-5.1.1/bin/shutdown.sh";
        execCmd.execCmd(stoprunm);
    }

    @Override
    public String selectInfo() {
        String tail = "tail -f /home/7d/jmeterlog.log";
        File file = new File("/home/7d/jmeterlog.log");
        String s = LogSvrReadInput.realtimeShowLog(file);
        logger.info("输出日志：--》{}",s);
        return s;
    }


}
