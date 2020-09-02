package com.techstar.monitordemo.Btrace;

import com.sun.btrace.BTraceUtils;
import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.OnMethod;
import static com.sun.btrace.BTraceUtils.jstack;

/**
 * 谁调用了execute方法？
 */
@BTrace
public class PrintMethodCallerTracer {
	@OnMethod(clazz = "com.techstar.monitordemo.controller.Adder", method = "execute")
	public static void traceExecute() {
		BTraceUtils.println("Adder.execute调用者为:");
		jstack();
	}
}
