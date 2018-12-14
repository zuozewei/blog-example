package com.zuozewei.springbootdemo.util;

import com.zuozewei.springbootdemo.entity.Result;

/**
 * 异常工具类
 */
public class ResultUtil {
	public static Result sucess(Object obj) {
		Result result = new Result();
		result.setCode(0);
		result.setMsg("sucess");
		result.setDate(obj);
		return result;
	}

	public static Result sucess() {
		Result result = new Result();
		result.setCode(0);
		result.setMsg("sucess");
		return result;
	}

	public static Result error(Integer code, String message) {
		Result result = new Result();
		result.setCode(code);
		result.setMsg(message);
		return result;
	}
}
