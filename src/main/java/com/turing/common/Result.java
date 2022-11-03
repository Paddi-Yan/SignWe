package com.turing.common;

import lombok.Builder;
import lombok.Data;

/**
 * @Author: 又蠢又笨的懒羊羊程序猿
 * @CreateTime: 2022年05月18日 19:53:35
 */
@Data
public class Result {
    private Object data;
    private String message;
    private Integer code;

    public static Result success() {
        Result result = new Result();
        result.setCode(HttpStatusCode.SUCCESS.getCode());
        result.setMessage(HttpStatusCode.SUCCESS.getMessage());
        return result;
    }

    public static Result success(Object data) {
        Result result = new Result();
        result.setData(data);
        result.setCode(HttpStatusCode.SUCCESS.getCode());
        result.setMessage(HttpStatusCode.SUCCESS.getMessage());
        return result;
    }

    public static Result success(HttpStatusCode httpStatusCode, String message) {
        Result result = new Result();
        result.setCode(httpStatusCode.getCode());
        result.setMessage(message);
        return result;
    }


    public static Result fail(HttpStatusCode httpStatusCode) {
        Result result = new Result();
        result.setCode(httpStatusCode.getCode());
        result.setMessage(httpStatusCode.getMessage());
        return result;
    }

    public static Result fail(HttpStatusCode httpStatusCode, String message) {
        Result result = new Result();
        result.setCode(httpStatusCode.getCode());
        result.setMessage(message);
        return result;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
