package com.turing.common;

/**
 * @Author: 又蠢又笨的懒羊羊程序猿
 * @CreateTime: 2022年01月20日 02:16:00
 */
public enum HttpStatusCode {
    SUCCESS(200, "请求已经成功处理"),
    Accepted(202, "请求已被服务器接受,但并未处理完成"),
    NO_CONTENT(204, "无内容,服务器成功处理,但未返回内容"),
    ERROR(500, "服务器内部发生错误"),
    NOT_FOUND(404, "请求路径不存在"),
    REQUEST_PARAM_ERROR(400, "请求携带参数错误"),
    FORBIDDEN(403, "权限不足,服务器拒绝处理该请求"),
    UNAUTHORIZED(401, "用户未通过身份验证");
    private Integer code;
    private String message;

    HttpStatusCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
