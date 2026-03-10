package com.ab.kkmalladminweb.exception;

/**
 * 业务异常
 *
 * @author KKMall
 * @since 2026-03-10
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}