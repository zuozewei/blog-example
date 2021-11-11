package com.sevendays.utils;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import com.sevendays.utils.sshlinx.LinuxShhread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.*;

/**
 * @author 7DGroup
 * @Title: MainCommand
 * @Description: java远程连接linux并发送命令
 * @date 2019/10/23 / 10:29
 */

public class MainCommand {
    private static final Logger log = LoggerFactory.getLogger(MainCommand.class);
    private static String DEFAULTCHART = "UTF-8";

    private static Connection sshhLoin(String ip, String username, String password) {
        boolean flag = false;
        Connection connection = null;
        try {
            connection = new Connection(ip);
            connection.connect();// 连接
            // 认证
            flag = connection.authenticateWithPassword(username, password);
            if (flag) {
                log.info("================登录成功==================");
                return connection;
            }
        } catch (IOException e) {
            log.info("=========登录失败=========" + e);
            connection.close();
            e.printStackTrace();
        }
        return connection;
    }


    /**
     * 远程执行shll脚本或者命令
     *  
     *
     * @param cmd            即将执行的命令
     * @return 命令执行完后返回的结果值
     */
    private static String execmd(Connection connection, String cmd) {
        String result = "";
        try {
            if (connection != null) {
                // 打开一个会话
                Session session = connection.openSession();
                // 执行命令
                session.execCommand(cmd);
                result = processStdout(session.getStdout(), DEFAULTCHART);
                System.out.println(result);
                // 如果为得到标准输出为空，说明脚本执行出错了
                if (StringUtils.isEmpty(result)) {
                    log.info("得到标准输出为空,链接conn:" + connection + ",执行的命令：" + cmd);
                    result = processStdout(session.getStderr(), DEFAULTCHART);
                } else {
                    log.info("执行命令成功,链接conn:" + connection + ",执行的命令：" + cmd);
                }
                connection.close();
                session.close();
            }
        } catch (IOException e) {
            log.info("执行命令失败,链接conn:" + connection + ",执行的命令：" + cmd + "  " + e);
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 解析脚本执行返回的结果集
     *
     * @param in                 输入流对象
     * @param charset            编码
     * @return 以纯文本的格式返回
     */
    private static String processStdout(InputStream in, String charset) {
        InputStream stdout = new StreamGobbler(in);
        StringBuffer buffer = new StringBuffer();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout, charset));
            String line = null;
            while ((line = br.readLine()) != null) {
                buffer.append(line + "\n");
                System.out.println(line);
            }
            br.close();
        } catch (UnsupportedEncodingException e) {
            log.info("解析脚本出错：" + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            log.info("解析脚本出错：" + e.getMessage());
            e.printStackTrace();
        }
        return buffer.toString();
    }

    public static void main(String[] args) {
        long currentTimeMillis = System.currentTimeMillis();
        String ip = "192.168.128.128";
        String username = "root";
        String password = "123456";
        String cmd = "uname -a";
        Connection connection = sshhLoin(ip, username, password);
        String execmd = execmd(connection, cmd);
        System.out.println(execmd);
        long currentTimeMillis1 = System.currentTimeMillis();
        log.info("ganymed-ssh2方式" + (currentTimeMillis1 - currentTimeMillis));

    }


}
