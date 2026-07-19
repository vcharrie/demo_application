package com.coreservice.application.exception;

public class TechnicalException extends RuntimeException {

    private final TechnicalError error;

    public TechnicalException(TechnicalError error) {
        super(error.message());
        this.error = error;
    }

    public TechnicalException(TechnicalError error, Throwable cause) {
        super(error.message(), cause);
        this.error = error;
    }

    public TechnicalError getError() {
        return error;
    }

    public String getCode() {
        return error.code();
    }
}
