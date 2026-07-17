package com.coreservice.domain.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String name) {
        super(name);
    }

}
