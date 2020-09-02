package com.techstar.monitordemo.Btrace;

import com.sun.btrace.BTraceUtils;
import com.sun.btrace.annotations.*;

import static com.sun.btrace.BTraceUtils.println;
import static com.sun.btrace.BTraceUtils.strcat;

/**
 * 有时候富有争议的一行代码不知道被谁调用，可以使用下面的代码来定位。
 */
@BTrace
public class PrintMethodLineTracer {
	@OnMethod(clazz = "com.techstar.monitordemo.controller.Adder", location = @Location(value = Kind.LINE, line = 15))
	public static void traceExecute(@ProbeClassName String pcn, @ProbeMethodName String pmn, int line) {
		BTraceUtils.println(strcat(strcat(strcat("调用者为: ", pcn), "."), pmn));
	}
}
