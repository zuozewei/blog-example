package com.zuozewei.javaagent01;

import java.lang.instrument.Instrumentation;

public class Agent {

//    public static void premain(String agentArgs) {
//        System.out.println("我是一个萌萌哒的 Java Agent");
//        try {
//            Thread.sleep(2000L);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

    public static void premain(String agentArgs, Instrumentation instrumentation)  {

        instrumentation.addTransformer(new ClassFileTransformerDemo());

        System.out.println("7DGroup Java Agent");
    }

}
