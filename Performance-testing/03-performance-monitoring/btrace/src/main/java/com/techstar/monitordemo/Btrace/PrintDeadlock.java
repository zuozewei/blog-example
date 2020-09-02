package com.techstar.monitordemo.Btrace;

import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.OnTimer;

import static com.sun.btrace.BTraceUtils.Threads.deadlocks;

/**
 * This BTrace program demonstrates deadlocks
 * built-in function. This example prints
 * deadlocks (if any) once every 4 seconds.
 */
@BTrace
public class PrintDeadlock {
	@OnTimer(4000)
	public static void print() {
		deadlocks();
	}
}
