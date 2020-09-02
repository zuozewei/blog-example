package com.techstar.monitordemo.Btrace;

import com.sun.btrace.BTraceUtils;
import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.Kind;
import com.sun.btrace.annotations.Location;
import com.sun.btrace.annotations.OnMethod;
import com.sun.btrace.annotations.ProbeClassName;
import com.sun.btrace.annotations.ProbeMethodName;

/**
 *打印方法执行行号
 */

@BTrace
public class PrintLine {

	@OnMethod(clazz = "com.techstar.monitordemo.controller.UserController", method = "exception", location = @Location(value = Kind.LINE))
	public static void anyRead(@ProbeClassName String pcn, @ProbeMethodName String pmn, int line) {
		BTraceUtils.println(pcn + "," + pmn + "," + line);
		BTraceUtils.println();
	}
}
