package com.techstar.monitordemo.Btrace;

import com.sun.btrace.BTraceUtils;
import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.OnMethod;
import com.sun.btrace.annotations.ProbeClassName;
import com.sun.btrace.annotations.ProbeMethodName;

/**
 * 拦截同名函数，通过参数区分
 */

@BTrace
public class PrintSame {

	@OnMethod(clazz = "com.techstar.monitordemo.controller.UserController", method = "same")
	public static void anyRead(@ProbeClassName String pcn, @ProbeMethodName String pmn, String name) {
		BTraceUtils.println(pcn + "," + pmn + "," + name);
		BTraceUtils.println();
	}
}
