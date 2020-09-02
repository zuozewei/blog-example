package com.techstar.monitordemo.Btrace;

import com.sun.btrace.BTraceUtils;
import com.sun.btrace.Profiler;
import com.sun.btrace.annotations.*;

/**
 *
 * Description:
 * This script demonstrates new capabilities built into BTrace 1.2
 * Shortened syntax - when omitting "public" identifier in the class
 * definition one can safely omit all other modifiers when declaring methods
 * and variables
 * Extended syntax for @ProbeMethodName annotation - you can use
 * parameter to request a fully qualified method name instead of
 * the short one
 * Profiling support - you can use {@linkplain Profiler} instance to gather
 * performance data with the smallest overhead possible
 */
@BTrace
class Profiling {
	@Property
	Profiler profiler = BTraceUtils.Profiling.newProfiler();

	@OnMethod(clazz = "/com\\.techstar\\..*/", method = "/.*/")
	void entry(@ProbeMethodName(fqn = true) String probeMethod) {
		BTraceUtils.Profiling.recordEntry(profiler, probeMethod);
	}

	@OnMethod(clazz = "/com\\.techstar\\..*/", method = "/.*/", location = @Location(value = Kind.RETURN))
	void exit(@ProbeMethodName(fqn = true) String probeMethod, @Duration long duration) {
		BTraceUtils.Profiling.recordExit(profiler, probeMethod, duration);
	}

	@OnTimer(5000)
	void timer() {
		BTraceUtils.Profiling.printSnapshot("Performance profile", profiler);
	}
}
