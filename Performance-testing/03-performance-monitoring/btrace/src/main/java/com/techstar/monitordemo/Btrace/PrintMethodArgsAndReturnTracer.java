package com.techstar.monitordemo.Btrace;

import com.sun.btrace.BTraceUtils;
import com.sun.btrace.annotations.*;
import com.techstar.monitordemo.controller.Adder;

import static com.sun.btrace.BTraceUtils.*;

/**
 * 调用此方法时传入的是什么参数，返回的是什么值，当时sleepTotalTime是多少？
 */
@BTrace
public class PrintMethodArgsAndReturnTracer {
	@OnMethod(clazz = "com.techstar.monitordemo.controller", method = "execute", location = @Location(Kind.RETURN))
	public static void traceExecute(@Self Adder instance, int arg1, int arg2, @Return int result) {
		BTraceUtils.println("Adder.execute");
		BTraceUtils.println(strcat("arg1 is:", str(arg1)));
		BTraceUtils.println(strcat("arg2 is:", str(arg2)));
		BTraceUtils.println(strcat("maxResult is:",
				str(get(field("com.techstar.monitordemo.controller", "maxResult"), instance))));
		BTraceUtils.println(strcat("return value is:", str(result)));
	}
}
