package com.sevendays.pojo;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liwen
 * @Title: Msg
 * @Description: 返回
 * @date 2019/11/17 / 13:14
 */
public class Msg {
    /**
     * 状态码   100-成功    200-失败
     */
    private int code;
    /**
     * 提示信息
     */
    private String msg;
    /**
     * 用户要返回给浏览器的数据
     */
    private Map<String, Object> extend = new HashMap<>(1);

    public static Msg success() {
        Msg result = new Msg();
        result.setCode(100);
        result.setMsg("处理成功！");
        return result;
    }
    /**
     * 失败返回
     *
     * @return
     */
    public static Msg fail() {
        Msg result = new Msg();
        result.setCode(200);
        result.setMsg("处理失败！");
        return result;
    }
    /**
     * 消息增加
     *
     * @param key
     * @param value
     * @return
     */
    public Msg add(String key, Object value) {
        this.getExtend().put(key, value);
        return this;
    }
    public void setCode(int code) {
        this.code = code;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
    public Map<String, Object> getExtend() {
        return extend;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
