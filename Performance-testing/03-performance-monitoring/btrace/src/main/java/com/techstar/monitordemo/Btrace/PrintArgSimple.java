package com.techstar.monitordemo.Btrace;

import com.sun.btrace.AnyType;
import com.sun.btrace.BTraceUtils;
import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.Kind;
import com.sun.btrace.annotations.Location;
import com.sun.btrace.annotations.OnMethod;
import com.sun.btrace.annotations.ProbeClassName;
import com.sun.btrace.annotations.ProbeMethodName;

/**
 * 拦截示例
 */
@BTrace
public class PrintArgSimple {

	@OnMethod(
			//类名
			clazz = "com.techstar.monitordemo.controller.UserController",
			//方法名
			method = "arg1",
			//拦截时刻：入口
			location = @Location(Kind.ENTRY))

	/**
	 * 拦截类名和方法名
	 */ public static void anyRead(@ProbeClassName String pcn, @ProbeMethodName String pmn, AnyType[] args) {
		BTraceUtils.printArray(args);
		BTraceUtils.println(pcn + "," + pmn);
		BTraceUtils.println();
	}
}
