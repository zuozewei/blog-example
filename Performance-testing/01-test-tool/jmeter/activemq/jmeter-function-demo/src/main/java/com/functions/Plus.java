package com.functions;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Plus extends AbstractFunction {

    //定义一个obect对象去接受传入变量值
    private Object[] values;
    //存储第一个和第二个参数
    private CompoundVariable first,second;

    /**
     * 执行方法
     * @param sampleResult
     * @param sampler
     * @return
     * @throws InvalidVariableException
     */
    public String execute(SampleResult sampleResult, Sampler sampler) throws InvalidVariableException {
        //接住第一个参数值
        first = (CompoundVariable) values[0];
        //接住第二个参数值
        second = (CompoundVariable) values[1];

        //计算两个参数的和
        int count = new Integer(first.execute().trim()) + new Integer(second.execute().trim());
        System.out.println("两个参数的和是："+count);

        //返回一个String类型的和
        return String.valueOf(count);
    }

    /**
     * 设置参数，接受用户传递的参数
     * @param collection
     * @throws InvalidVariableException
     */
    public void setParameters(Collection<CompoundVariable> collection) throws InvalidVariableException {
        //检查参数是否合法
        checkParameterCount(collection,2);
        //转换成数组
        values = collection.toArray();
    }

    /**
     * 函数名称
     * @return
     */
    public String getReferenceKey() {
        return "__7DDemoPlus";
    }

    /**
     * 函数描述，获取参数描述
     * @return
     */
    public List<String> getArgumentDesc() {
        List desc = new ArrayList();
        //界面上显示两行参数描述
        desc.add("第一个数字");
        desc.add("第二个数字");

        return desc;
    }
}
