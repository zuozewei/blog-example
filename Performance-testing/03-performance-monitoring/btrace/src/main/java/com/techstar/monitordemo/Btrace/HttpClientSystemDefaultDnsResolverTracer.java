package com.techstar.monitordemo.Btrace;

import com.sun.btrace.BTraceUtils;
import com.sun.btrace.annotations.*;

import static com.sun.btrace.BTraceUtils.*;

@BTrace
public class HttpClientSystemDefaultDnsResolverTracer {
	@TLS
	static long beginTime;

	@OnMethod(clazz = "org.apache.http.impl.conn.SystemDefaultDnsResolver", method = "resolve")
	public static void traceGetByNameBegin() {
		beginTime = timeMillis();
	}

	@OnMethod(clazz = "org.apache.http.impl.conn.SystemDefaultDnsResolver", method = "resolve", location = @Location(Kind.RETURN))
	public static void traceGetByNameEnd() {
		BTraceUtils.println(strcat(strcat("org.apache.http.impl.conn.SystemDefaultDnsResolver.resolve 耗时:",
				str(timeMillis() - beginTime)), "ms"));
	}
}
