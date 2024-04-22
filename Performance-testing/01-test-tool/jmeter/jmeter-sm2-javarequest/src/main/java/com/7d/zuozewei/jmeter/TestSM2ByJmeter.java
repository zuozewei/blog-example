package com.7d.zuozewei.jmeter;

import com.7d.zuozewei.until.SMEncry;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

/**
 * @author zuozewei
 */

@Slf4j
public class TestSM2ByJmeter extends AbstractJavaSamplerClient {

    /**
     * 设置传入界面的参数
     * @return
     */

    @Override
    public Arguments getDefaultParameters(){
        Arguments arguments = new Arguments();
        arguments.addArgument("text","输入报文");
        arguments.addArgument("publicKey","输入密钥");
        return arguments;
    }

    private String text;
    private String publicKey;
    private String ciphertext;

    /**
     * 初始化方法
     * @return
     */

    @Override
    public void setupTest(JavaSamplerContext context){
        //获取Jmeter中设置的参数
         text = context.getParameter("text");
         publicKey = context.getParameter("publicKey");
         log.info("text is:"+text);
         log.info("publicKey is:"+publicKey);
    }

    /**
     * 性能测试线程运行体
     * @return
     */

    @Override
    public SampleResult runTest(JavaSamplerContext context) {

        SampleResult result = new SampleResult();
        // 设置请求名称
        result.setSampleLabel("Jmeter SM2 Java Request");

        //开始统计响应时间标记
        result.sampleStart();

        try {
            long begin = System.currentTimeMillis();

            log.info("-----------------开始执行加密请求-----------------！");

            //调用加密
            ciphertext = SMEncry.sm2Encrypt(text,publicKey);

            long cost = (System.currentTimeMillis() - begin);

            //打印时间戳差值。Java请求响应时间
            System.out.println(ciphertext+" 总计花费：["+cost+"ms]");

            if (ciphertext == null){
                //设置测试结果为fasle
                result.setSuccessful(false);
                return result;
            }
            if (ciphertext.length() != 0){
                //设置测试结果为true
                result.setSuccessful(true);
            }else{
                //设置测试结果为fasle
                result.setSuccessful(false);
                result.setResponseMessage("ERROR");
            }
        }catch (Exception e){
            result.setSuccessful(false);
            result.setResponseMessage("ERROR");
            e.printStackTrace();
        }finally {
            //结束统计响应时间标记
            result.sampleEnd();
            log.info("-----------------结束执行加密请求-----------------！");
        }
        //将请求结果放进result中
        result.setResponseData(ciphertext,"UTF-8");
        //设置响应结果类型
        result.setDataType(SampleResult.TEXT);
        //改变查看结果树中显示的名称
        result.setSampleLabel("自定义SM2加密请求");
        return result;
    }

}
