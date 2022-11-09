package com.turing.exception;

import java.util.Map;

/**
 * @Project: SignWe
 * @Author: Paddi-Yan
 * @CreatedTime: 2022年11月09日 15:22:57
 */
public class RequestParamValidationException extends BaseException{

    public RequestParamValidationException(Map<String, Object> data) {
        super(ErrorCode.REQUEST_VALIDATION_FAILED, data);
    }

    public RequestParamValidationException() {
        super(ErrorCode.REQUEST_VALIDATION_FAILED, null);
    }

}
