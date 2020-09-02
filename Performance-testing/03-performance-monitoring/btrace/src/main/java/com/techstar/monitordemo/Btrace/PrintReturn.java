package com.techstar.monitordemo.Btrace;

import com.sun.btrace.AnyType;
import com.sun.btrace.BTraceUtils;
import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.Kind;
import com.sun.btrace.annotations.Location;
import com.sun.btrace.annotations.OnMethod;
import com.sun.btrace.annotations.ProbeClassName;
import com.sun.btrace.annotations.ProbeMethodName;
import com.sun.btrace.annotations.Return;

/**
 * 拦截返回值
 */
@BTrace
public class PrintReturn {

	@OnMethod(clazz = "com.techstar.monitordemo.controller.UserController", method = "arg1",
			//拦截时刻：返回值
			location = @Location(Kind.RETURN))
	public static void anyRead(@ProbeClassName String pcn, @ProbeMethodName String pmn, @Return AnyType result) {
		BTraceUtils.println(pcn + "," + pmn + "," + result);
		BTraceUtils.println();
	}
}
