package com.sevendays.utils.sshlinx;

import com.jcraft.jsch.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.UUID;

/**
 * @author 7DGroup
 * @Title: LinuxShhread
 * @Description: 实时读取日志
 * @date 2019/11/13 / 14:01
 */

public class LinuxShhread {
    private static final Logger log = LoggerFactory.getLogger(LinuxShhread.class);
    /**用户名*/
    final static String userName = "root";
    /**密码*/
    final static String password = "thtest";
    /** 服务器地址*/
    final static String host = "192.168.129.43";
    // 端口号
    final static int port = 22;
    final static int timeout = 60000000;

    private static Session session;


    public static void main(String[] args) {
        //jmeter  -n -t ${filename}.jmx -l ${filename}.jtl -j ${filename}.log -e -o ${filename}
        String jmeterCmd = "jmeter -n -t /home/jessica/httpThread.jmx -l log.jtl";
        String cmd = "uname -a && date && uptime && who && vmstat 1 60 ";
        /*try {

            String rs =   LinuxShh.exeCommand(host,port,userName,password,cmd);
            System.out.println(rs);

        } catch (JSchException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        // 根据用户名，主机ip，端口获取一个Session对象
        LinuxShhread.ConnetList(host, port, userName, password);
        //String cmd = "vmstat 1 1";// 要运行的命
        //>/home/linux_shell/m/vmstat.txt
        getLog("uptime");
    }

    /**
     * 链接
     *
     * @param host
     * @param port
     * @param user
     * @param password
     * @return
     */
    static Session ConnetList(String host, int port, String user, String password) {
        // 创建JSch对象
        JSch jsch = new JSch();
        try {
            session = jsch.getSession(user, host, port);
            // 设置密码
            session.setPassword(password);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            // 为Session对象设置properties
            session.setConfig(config);
            // 设置timeout时间
            session.setTimeout(timeout);
            // 通过Session建立链接
            session.connect();
        } catch (JSchException e) {
            e.printStackTrace();
        }
        return session;
    }

    /**
     * 实时获取命令日志
     *
     * @param command
     * @throws JSchException
     * @throws IOException
     */
    public static void getLog(String command) {
        try {
            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            channelExec.setInputStream(null);
            channelExec.setErrStream(System.err);
            channelExec.connect();
            InputStream in = channelExec.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
            //写入本地的文件
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("d:\\2.txt"), "UTF-8"));
            String buf = null;
            StringBuffer sb = new StringBuffer();
            log.info("您的IP是：" + host);
            log.info("您的IP是：" + host);
            log.info("以下是：系统资源信息：");
            while ((buf = reader.readLine()) != null) {
                sb.append(buf);
                //写入相关文件
                out.write(buf);
                out.newLine();
                // 打印控制台输出
                System.out.println(buf);
            }
            //清楚缓存
            out.flush();
            //关闭流
            reader.close();
            out.close();
            channelExec.disconnect();
            if (null != session) {
                session.disconnect();
            }
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 全部结果后，才显示处理，不是实时获取数据
     *
     * @param host
     * @param port
     * @param user
     * @param password
     * @param command
     * @return
     * @throws JSchException
     * @throws IOException
     */
    public static String exeCommand(String host, int port, String user, String password, String command) throws JSchException, IOException {

        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host, port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(password);
        session.connect();
        // 设置timeout时间
        session.setTimeout(timeout);
        ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
        InputStream in = channelExec.getInputStream();
        channelExec.setCommand(command);
        channelExec.setErrStream(System.err);
        channelExec.connect();
        String out = IOUtils.toString(in, "UTF-8");
        channelExec.disconnect();
        session.disconnect();
        return out;
    }


    /**
     * 连接服务器
     *
     * @param username
     * @param passwd
     * @param host
     * @param port
     */
    private static void connect(String username, String passwd, String host, int port) {
        try {
            JSch jsch = new JSch();
            //获取sshSession
            session = jsch.getSession(username, host, port);
            //添加密码
            session.setPassword(passwd);
            Properties sshConfig = new Properties();
            //严格主机密钥检查
            sshConfig.put("StrictHostKeyChecking", "no");
            session.setConfig(sshConfig);
            //开启sshSession连接
            session.connect();
            log.info("Server connection successful.");
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }


    /**
     * 执行相关命令
     *
     * @param command
     * @param username
     * @param passwd
     * @param host
     * @param port
     * @param outFilePath
     * @return
     */
    public static String execCmd(String command, String username, String passwd, String host, int port, String outFilePath) {

        String resultJson = null;
        ChannelExec channelExec = null;
        if (command != null) {
            try {
                connect(username, passwd, host, port);
                channelExec = (ChannelExec) session.openChannel("exec");
                // 设置需要执行的shell命令
                channelExec.setCommand(command);
                log.info("linux命令:" + command);
                channelExec.setInputStream(null);
                channelExec.setErrStream(System.err);
                channelExec.connect();
                //读数据
                resultJson = getServerData(host, port, username, passwd, outFilePath);
            } catch (JSchException e) {
                e.printStackTrace();
            } finally {
                if (null != channelExec) {
                    channelExec.disconnect();
                }
            }
        }
        return resultJson;
    }

    /**
     * 从 服务器 获取 数据
     *
     * @param host
     * @param port
     * @param username
     * @param password
     * @param filePath
     * @return
     */
    private static String getServerData(String host, int port, String username, String password, String filePath) {
        ChannelSftp sftp = null;
        StringBuffer buffer = new StringBuffer();
        try {

            if (!session.isConnected()) {
                connect(username, password, host, port);
            }

            //获取sftp通道
            Channel channel = session.openChannel("sftp");
            //开启
            channel.connect();
            sftp = (ChannelSftp) channel;
            log.info("Connected to " + host + ".");
            //获取生成文件流
            InputStream inputStream = sftp.get(filePath);
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
            //关闭流
            inputStream.close();
            in.close();

            log.info(" 执行结果为: " + buffer.toString());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        } finally {
            if (null != sftp) {
                sftp.quit();
            }
            closeSession();
        }
        return buffer.toString();
    }


    public static void closeSession() {
        if (session != null) {
            session.disconnect();
        }

    }

    public static String getUUID() {
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        return str.replace("-", "");
    }


}
