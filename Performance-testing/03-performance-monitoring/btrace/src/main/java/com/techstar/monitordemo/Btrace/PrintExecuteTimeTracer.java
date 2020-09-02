package com.techstar.monitordemo.Btrace;

import com.sun.btrace.BTraceUtils;
import com.sun.btrace.annotations.*;

import static com.sun.btrace.BTraceUtils.*;

/**
 * 追踪某个方法的执行时间，实现原理同AOP一样。
 */
@BTrace
public class PrintExecuteTimeTracer {
	@TLS
	static long beginTime;

	@OnMethod(clazz = "com.techstar.monitordemo.controller.Adder", method = "execute")
	public static void traceExecuteBegin() {
		beginTime = timeMillis();
	}

	@OnMethod(clazz = "com.techstar.monitordemo.controller.Adder", method = "execute", location = @Location(Kind.RETURN))
	public static void traceExecute(int arg1, int arg2, @Return int result) {
		BTraceUtils.println(strcat(strcat("Adder.execute 耗时:", str(timeMillis() - beginTime)), "ms"));
		BTraceUtils.println(strcat("返回结果为:", str(result)));
	}
}
