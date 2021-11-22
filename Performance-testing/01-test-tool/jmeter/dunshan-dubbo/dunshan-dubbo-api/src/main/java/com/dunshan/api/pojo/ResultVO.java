package com.dunshan.api.pojo;

import lombok.Getter;

import java.io.Serializable;

/**
 * @author 7DGroup
 * @version 1.0
 * @Date: 2021-05-04 11:50
 * @Description: 统一结果响应
 */
@Getter
public class ResultVO<T> implements Serializable {

    /**
     * 返回码
     */
    private int code;
    /**
     * 返回信息
     */
    private String message;
    /**
     * 返回数据
     */
    private T data;

    /**
     * 私有化构造器
     **/
    private ResultVO() {
    }

    private ResultVO(ResultVO<T> resultVO) {
        this.code = resultVO.code;
        this.message = resultVO.message;
        this.data = resultVO.data;
    }

    /**
     * Build
     */
    public static class Builder<T> {
        private ResultVO<T> resultVO;

        public Builder() {
            resultVO = new ResultVO<>();
        }

        public Builder code(int code) {
            resultVO.code = code;
            return this;
        }

        public Builder message(String message) {
            resultVO.message = message;
            return this;
        }

        public Builder data(T data) {
            resultVO.data = data;
            return this;
        }

        public ResultVO<T> build() {
            return new ResultVO<>(resultVO);
        }
    }
}
