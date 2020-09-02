package com.techstar.monitordemo.Btrace;

import java.lang.reflect.Field;

import com.sun.btrace.BTraceUtils;
import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.Kind;
import com.sun.btrace.annotations.Location;
import com.sun.btrace.annotations.OnMethod;
import com.sun.btrace.annotations.ProbeClassName;
import com.sun.btrace.annotations.ProbeMethodName;
import com.techstar.monitordemo.domain.User;

/**
 * 拦截复杂参数
 */
@BTrace
public class PrintArgComplex {


	@OnMethod(clazz = "com.techstar.monitordemo.controller.UserController", method = "arg2", location = @Location(Kind.ENTRY))
	public static void anyRead(@ProbeClassName String pcn, @ProbeMethodName String pmn, User user) {
		//print all fields
		BTraceUtils.printFields(user);
		//print one field
		Field filed2 = BTraceUtils.field("com.techstar.monitordemo.domain.User", "name");
		BTraceUtils.println(BTraceUtils.get(filed2, user));
		BTraceUtils.println(pcn + "," + pmn);
		BTraceUtils.println();
	}
}
