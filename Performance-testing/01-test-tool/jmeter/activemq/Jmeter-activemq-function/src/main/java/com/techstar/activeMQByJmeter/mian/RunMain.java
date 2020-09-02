package com.techstar.activeMQByJmeter.mian;

import com.techstar.activeMQByJmeter.common.Producer;

/**
 * 测试类
 */
public class RunMain {
    public static void main(String[] args) {
        new Producer().producer
                ("tcp://192.168.1.5:61616","7DQueue","7DTest...");
    }
}
