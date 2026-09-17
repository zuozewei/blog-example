package com.openvpp.common.api;

import java.io.Serializable;

/**
 * 统一返回体。全平台所有 Controller 只返回这一种结构，
 * 前端拦截器按 code 统一处理，业务代码不再各自包错。
 */
public class ApiResult<T> implements Serializable {

    public static final int SUCCESS = 0;

    private int code;
    private String message;
    private T data;

    public ApiResult() {
    }

    private ApiResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(SUCCESS, "success", data);
    }

    public static <T> ApiResult<T> error(int code, String message) {
        return new ApiResult<>(code, message, null);
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
