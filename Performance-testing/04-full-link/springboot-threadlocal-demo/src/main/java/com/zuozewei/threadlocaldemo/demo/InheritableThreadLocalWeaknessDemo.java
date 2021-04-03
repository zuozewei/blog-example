package com.zuozewei.threadlocaldemo.demo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 演示InheritableThreadLocal的缺陷
 * @author: zuozewei
 * @date: 2020-4-4
 */

public class InheritableThreadLocalWeaknessDemo {

    private static final InheritableThreadLocal<Integer> INHERITABLE_THREAD_LOCAL = new InheritableThreadLocal<>();
    //模拟业务线程池
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(5);

    public static void main(String[] args) throws InterruptedException {
        //模拟同时10个web请求，一个请求一个线程
        for (int i = 0; i < 10; i++) {
            new TomcatThread(i).start();
        }

        Thread.sleep(3000);
        threadPool.shutdown();
    }

    static class TomcatThread extends Thread{
        //线程下标
        int index;

        public TomcatThread(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            String parentThreadName = Thread.currentThread().getName();
            //父线程中将index值塞入线程上下文变量
            System.out.println( parentThreadName+ ":" + index);
            INHERITABLE_THREAD_LOCAL.set(index);

            threadPool.submit(new BusinessThread(parentThreadName));
        }
    }

    static class BusinessThread implements Runnable{
        //父进程名称
        private String parentThreadName;

        public BusinessThread(String parentThreadName) {
            this.parentThreadName = parentThreadName;
        }

        @Override
        public void run() {
            System.out.println("parent:"+parentThreadName+":"+INHERITABLE_THREAD_LOCAL.get());
        }
    }
}
