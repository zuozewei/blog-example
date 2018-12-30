package com.zuozewei.logbackdemo.config;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 日志注解类
 * @author zuozewei
 *
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {
	/**
	 * 日志描述，这里使用了@AliasFor 别名。spring提供的
	 * @return
	 */
	@AliasFor("desc")
	String value() default "";
	
	/**
	 * 日志描述
	 * @return
	 */
	@AliasFor("value")
	String desc() default "";
	
	/**
	 * 是否忽略
	 * @return
	 */
	boolean ignore() default false;
}
