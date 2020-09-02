package com.techstar.monitordemo.Btrace;

import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.OnLowMemory;

import java.lang.management.MemoryUsage;

import static com.sun.btrace.BTraceUtils.println;

/**
 * This sample traces memory threshold exceeds.
 * You need to specify the memory pool to watch
 * out and the usage threshold. You can write
 * script that dumps heap by dumpHeap on crossing
 * the threshold instead of just printing a message.
 * Note that the name of the old gen is dependent on
 * GC algorithm. With ParallelGC, the name is "PS Old Gen".
 */
@BTrace
public class PrintMemAlerter {
	@OnLowMemory(pool = "Tenured Gen", threshold = 6000000)
	public static void onLowMem(MemoryUsage mu) {
		println(mu);
	}
}
