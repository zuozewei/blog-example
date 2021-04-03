package com.zuozewei.threadlocaldemo.demo;

/**
 * @author: zuozwewei
 * @date: 2021-4-2 13:13
 */

public class InheritableThreadLocalDemo {
    private static final InheritableThreadLocal<Integer> requestIdThreadLocal = new InheritableThreadLocal<>();
    public static void main(String[] args) {
        Integer reqId = new Integer(5);
        InheritableThreadLocalDemo threadLocalExample = new InheritableThreadLocalDemo();
        threadLocalExample.setRequestId(reqId);
    }

    public void setRequestId(Integer requestId) {
        requestIdThreadLocal.set(requestId);
        doBussiness();
    }

    public void doBussiness() {
        System.out.println("首先打印requestId:" + requestIdThreadLocal.get());
        (new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("子线程启动");
                System.out.println("在子线程中访问requestId:" + requestIdThreadLocal.get());
            }
        })).start();
    }
}