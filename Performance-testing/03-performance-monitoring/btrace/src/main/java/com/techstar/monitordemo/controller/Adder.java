package com.techstar.monitordemo.controller;

import java.util.Random;

public class Adder {
	private static int maxResult = 0;

	public int execute(int arg1, int arg2) throws Exception {
		System.out.println("arg1: " + arg1 + ",arg2: " + arg2);
		int result = arg1 + arg2;
		if (result > maxResult)
			maxResult = result;
		System.out.println("result: " + result + ",maxResult: " + maxResult);
		Thread.sleep(result);
		return result;
	}

	public void runMain() throws Exception {
		Random random = new Random();
		while (true) {
			execute(random.nextInt(1000), random.nextInt(1000));
			Thread.sleep(2000);
		}
	}
}
