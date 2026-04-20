package com.coreservice.exception;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.coreservice.domain.exception.ResourceNotFoundException;
import com.coreservice.exception.mapper.SecurityErrorMapper;
import com.coreservice.exception.mapper.SecurityErrorMapper.SecurityError;

import jakarta.validation.ConstraintViolationException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Environment env;

    public GlobalExceptionHandler(Environment env) {
        this.env = env;
    }    

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "VALIDATION_ERROR",
                        "message", "Invalid request",
                        "details", errors
                ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getConstraintViolations().forEach(v ->
                errors.put(v.getPropertyPath().toString(), v.getMessage())
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "VALIDATION_ERROR",
                        "message", "Invalid request",
                        "details", errors
                ));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Object> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "NOT_FOUND",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "BAD_REQUEST",
                        "message", ex.getMessage()
                ));
    }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<Object> handleGeneric(Exception ex) {

        // En mode test/dev → log complet
        if (isDevOrTestProfile()) {
                // Stacktrace complète
                ex.printStackTrace();
        } else {
                // En prod → log minimal, sans fuite d’infos
                // log.error("Unexpected error occurred: {}", sanitize(ex.getMessage()));
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "INTERNAL_ERROR",
                        "message", "Unexpected error occurred"
                ));
        }

        private boolean isDevOrTestProfile() {
        return Arrays.asList(env.getActiveProfiles()).stream()
                .anyMatch(p -> p.equals("dev") || p.equals("test"));
        }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(ResourceNotFoundException ex) {
    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(Map.of(
                    "error", "NOT_FOUND",
                    "message", ex.getMessage()
            ));
    }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<SecurityError> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(SecurityErrorMapper.from(ex));
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<SecurityError> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(SecurityErrorMapper.from(ex));
        }

}
