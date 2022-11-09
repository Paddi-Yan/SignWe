package com.turing.exception;

import java.util.Map;

/**
 * @Project: SignWe
 * @Author: Paddi-Yan
 * @CreatedTime: 2022年11月09日 15:30:25
 */
public class InternalServerException extends BaseException{
    public InternalServerException(Map<String, Object> data) {
        super(ErrorCode.ERROR, data);
    }

    public InternalServerException() {
        super(ErrorCode.ERROR, null);
    }

}
