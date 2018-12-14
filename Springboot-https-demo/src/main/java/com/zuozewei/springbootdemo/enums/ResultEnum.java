package com.zuozewei.springbootdemo.enums;

/**
 * 封装异常消息枚举类
 */

public enum ResultEnum {
	UNKONW_ERROR(-1, "未知错误"),
	SUCCESS(0, "成功");

	private Integer code;
	private String msg;

	ResultEnum(Integer code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public Integer getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}
}
