package com.techstar.monitordemo.Btrace;

import com.sun.btrace.BTraceUtils;
import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.OnMethod;
import com.sun.btrace.annotations.ProbeClassName;
import com.sun.btrace.annotations.ProbeMethodName;

/**
 * 正则匹配方法
 */
@BTrace
public class PrintRegex {

	@OnMethod(clazz = "com.techstar.monitordemo.controller.UserController", method = "/.*/")
	public static void anyRead(@ProbeClassName String pcn, @ProbeMethodName String pmn) {
		BTraceUtils.println(pcn + "," + pmn);
		BTraceUtils.println();
	}
}
