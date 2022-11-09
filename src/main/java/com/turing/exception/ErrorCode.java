package com.turing.exception;

import org.springframework.http.HttpStatus;

/**
 * @Project: SignWe
 * @Author: Paddi-Yan
 * @CreatedTime: 2022年11月09日 14:14:49
 */
public enum ErrorCode {
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,"未能找到该资源"),
    REQUEST_VALIDATION_FAILED(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "请求数据校验失败"),
    FORBIDDEN(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN, "权限不足,服务器拒绝处理该请求"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED, "用户未通过身份验证"),
    ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部发生错误");

    private final int code;

    private final HttpStatus status;

    private final String message;

    ErrorCode(int code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    @Override
    public String toString() {
        return "ErrorCode{" +
                "code=" + code +
                ", status=" + status +
                ", message='" + message + '\'' +
                '}';
    }

    public int getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
