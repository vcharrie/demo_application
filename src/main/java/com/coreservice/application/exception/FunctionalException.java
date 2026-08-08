package com.coreservice.application.exception;

public class FunctionalException extends RuntimeException {

    private final FunctionalError error;

    public FunctionalException(FunctionalError error, Object... args) {
        super(String.format(error.template(), args));
        this.error = error;
    }

    public FunctionalError getError() {
        return error;
    }

    public String getCode() {
        return error.code();
    }
}
