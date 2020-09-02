package com.techstar.zuozewei.jmeter;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

public class RunMain {
    public static void main(String[] args) {
        Arguments arguments = new Arguments();
        arguments.addArgument("ip","172.16.14.251");
        arguments.addArgument("port","9999");
        arguments.addArgument("X","1");
        arguments.addArgument("Y","3");
        arguments.addArgument("type","0");
        JavaSamplerContext context = new JavaSamplerContext(arguments);
        TestThriftByJmeter jmeter = new TestThriftByJmeter();

        jmeter.setupTest(context);
        jmeter.runTest(context);
        jmeter.tearDownTest(context);

    }
}


