package com.coreservice.application.exception;


public class TechnicalException extends RuntimeException {

    private final TechnicalError error;

    public TechnicalException(TechnicalError error, Object... args) {
        super(String.format(error.template(), args));
        this.error = error;
    }

    public TechnicalError getError() {
        return error;
    }

    public String getCode() {
        return error.code();
    }
}
