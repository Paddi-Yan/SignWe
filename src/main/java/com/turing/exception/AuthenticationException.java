package com.turing.exception;

import java.util.Map;

/**
 * @Project: SignWe
 * @Author: Paddi-Yan
 * @CreatedTime: 2022年11月09日 15:12:38
 */
public class AuthenticationException extends BaseException{
    public AuthenticationException(Map<String, Object> data) {
        super(ErrorCode.UNAUTHORIZED, data);
    }

    public AuthenticationException() {
        super(ErrorCode.UNAUTHORIZED, null);
    }
}
