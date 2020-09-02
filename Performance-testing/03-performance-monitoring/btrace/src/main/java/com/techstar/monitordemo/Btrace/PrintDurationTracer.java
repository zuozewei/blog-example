package com.techstar.monitordemo.Btrace;

/**
 * 探测某个包路径下的方法执行时间是否超过某个阈值的程序，如果超过了该阀值，则打印当前线程的栈信息。
 */

import com.sun.btrace.BTraceUtils;
import com.sun.btrace.annotations.*;

import static com.sun.btrace.BTraceUtils.*;

@BTrace
public class PrintDurationTracer {
	@OnMethod(clazz = "/com\\.techstar\\.monitordemo\\..*/", method = "/.*/", location = @Location(Kind.RETURN))
	public static void trace(@ProbeClassName String pcn, @ProbeMethodName String pmn, @Duration long duration) {
		//duration的单位是纳秒
		if (duration > 1000 * 1000 * 2) {
			BTraceUtils.println(Strings.strcat(Strings.strcat(pcn, "."), pmn));
			BTraceUtils.print(" 耗时:");
			BTraceUtils.print(duration);
			BTraceUtils.println("纳秒,堆栈信息如下");
			jstack();
		}
	}
}

