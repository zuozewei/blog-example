package com.techstar.zuozewei.jmeter;

import com.techstar.zuozewei.client.ThriftClient;
import com.techstar.zuozewei.service.ComputeRequest;
import com.techstar.zuozewei.service.ComputeResponse;
import com.techstar.zuozewei.service.ComputeType;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

public class TestThriftByJmeter extends AbstractJavaSamplerClient {
    private ThriftClient client;
    private ComputeRequest request;
    private ComputeResponse response;

    //设置传入界面的参数
    @Override
    public Arguments getDefaultParameters(){
        Arguments arguments = new Arguments();
        arguments.addArgument("ip","172.16.14.251");
        arguments.addArgument("port","9999");
        arguments.addArgument("X","0");
        arguments.addArgument("Y","0");
        arguments.addArgument("type","0");
        return arguments;
    }

    //初始化方法
    @Override
    public void setupTest(JavaSamplerContext context){
        //获取Jmeter中设置的参数
        String ip = context.getParameter("ip");
        int port = context.getIntParameter("port");
        int x = context.getIntParameter("X");
        int y = context.getIntParameter("Y");
        ComputeType type = ComputeType.findByValue(context.getIntParameter("type"));

        //创建客户端
        client = new ThriftClient(ip,port);
        //设置request请求
        request = new ComputeRequest(x,y,type);
        super.setupTest(context);
    }

    //性能测试线程运行体
    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        SampleResult result = new SampleResult();
        //开始统计响应时间标记
        result.sampleStart();
        try {
            long begin = System.currentTimeMillis();
            response = client.getResponse(request);
            long cost = (System.currentTimeMillis() - begin);
            //打印时间戳差值。Java请求响应时间
            System.out.println(response.toString()+" 总计花费：["+cost+"ms]");

            if (response == null){
                //设置测试结果为fasle
                result.setSuccessful(false);
                return result;
            }
            if (response.getErrorNo() == 0){
                //设置测试结果为true
                result.setSuccessful(true);
            }else{
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
        }
        return result;
    }

    //测试结束方法
    public void tearDownTest(JavaSamplerContext context) {
        if (client != null) {
            client.close();
        }

        super.teardownTest(context);
    }

}
