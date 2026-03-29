package com.coreservice.domain.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String id) {
        super("Resource not found: " + id);
    }
}

