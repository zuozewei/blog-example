package com.zuozewei.springbootdemo.entity;

import lombok.Data;

/**
 * 封装异常对象（Http请求返回的最外层对象）
 * @param <T>
 */

@Data
public class Result<T> {
    //错误码
    private Integer code;
    //提示信息
    private String msg;
    private T date;
}
