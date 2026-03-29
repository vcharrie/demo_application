package com.coreservice.domain.exception;

public class ResourceConflictException extends RuntimeException {
    public ResourceConflictException(String name) {
        super("Resource with name already exists: " + name);
    }
}

