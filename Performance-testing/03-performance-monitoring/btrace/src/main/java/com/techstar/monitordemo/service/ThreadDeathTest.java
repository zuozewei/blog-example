package com.techstar.monitordemo.service;

public class ThreadDeathTest {
    private static int resource = 0;

    public static synchronized void monitor(String threadName) {
        System.out.println("*****************");
        try {
            System.out.println(threadName + "进入管程");
            System.out.println(threadName + "开始操作互斥资源");
            resource += 1;
            Thread.sleep(5000);
            resource -= 1;
            System.out.println(threadName + "互斥资源操作完成");
            System.out.println(threadName + "退出管程");
        } catch (Throwable ex) {
            System.out.println("捕获非检查异常:" + ex);
//            ex.printStackTrace();
        }
    }


    public static void main(String[] args) {
        try {
            Thread thread1 = new Thread() {
                public void run() {
                    monitor("Thread1");
                }
            };
            thread1.start();
            Thread thread2 = new Thread() {
                public void run() {
                    monitor("Thread2");
                }
            };
            thread2.start();
            //给充足的时间，让线程启动
            Thread.sleep(3000);
            thread2.join();
            System.out.println("------------------");
            System.out.println("互斥资源期待值为：" + 0);
            System.out.println("互斥资源实际值为：" + resource);
        } catch (Throwable t) {
            System.out.println("Caught in main: " + t);
        }
    }
}
