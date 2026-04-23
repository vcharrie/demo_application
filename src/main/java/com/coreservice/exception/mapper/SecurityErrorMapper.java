package com.coreservice.exception.mapper;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

public final class SecurityErrorMapper {

    public record SecurityError(String error, String message) {}

    public static SecurityError from(AuthenticationException ex) {
        return new SecurityError("UNAUTHORIZED", ex.getMessage());
    }

    public static SecurityError from(AccessDeniedException ex) {
        return new SecurityError("FORBIDDEN", ex.getMessage());
    }
}
