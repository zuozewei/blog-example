package com.techstar.activeMQByJmeter.functions;

import com.techstar.activeMQByJmeter.common.Producer;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * ActiveMQ扩展函数
 */
public class ActiveMQFunction extends AbstractFunction{
    //定义一个obect对象去接受传入变量值
    private Object[] values;
    //存储三个参数
    private CompoundVariable brokerURL,Queue,message;

    /**
     * 执行方法
     * @param sampleResult
     * @param sampler
     * @return
     * @throws InvalidVariableException
     */
    public String execute(SampleResult sampleResult, Sampler sampler) throws InvalidVariableException {
        //接住服务地址URL参数值
        brokerURL = (CompoundVariable) values[0];
        //接住消息队列参数值
        Queue = (CompoundVariable) values[1];
        //接住消息内容参数值
        message = (CompoundVariable) values[2];

        //把参数类型转成字符串
        String strBrokerURL = brokerURL.execute().toString();
        String strQueue = Queue.execute().toString();
        String strMessage = message.execute().toString();

        //指定目的地，发送消息
        new Producer().producer(strBrokerURL,strQueue,strMessage);

        //返回值
        return null;
    }

    /**
     * 设置参数，接受用户传递的参数
     * @param collection
     * @throws InvalidVariableException
     */
    public void setParameters(Collection<CompoundVariable> collection) throws InvalidVariableException {
        //检查参数是否合法
        checkParameterCount(collection,3);
        //转换成数组
        values = collection.toArray();
    }

    /**
     * 函数名称
     * @return
     */
    public String getReferenceKey() {
        return "__7DGroupActiveMQ";
    }

    /**
     * 函数描述，获取参数描述
     * @return
     */
    public List<String> getArgumentDesc() {
        List desc = new ArrayList();
        //界面上显示两行参数描述
        desc.add("服务地址");
        desc.add("消息队列");
        desc.add("消息内容");

        return desc;
    }
}