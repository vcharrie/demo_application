package com.coreservice.domain.exception;

public class BusinessException extends RuntimeException {

    private final BusinessError error;

    public BusinessException(BusinessError error, Object... args) {
        super(String.format(error.template(), args));
        this.error = error;
    }

    public BusinessError getError() {
        return error;
    }

    public String getCode() {
        return error.code();
    }
}