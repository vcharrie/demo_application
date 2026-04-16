package com.coreservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.coreservice.domain.exception.ResourceNotFoundException;
import com.coreservice.exception.mapper.ValidationErrorMapper;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {

        List<ValidationErrorMapper.ValidationError> errors = ValidationErrorMapper.from(ex);

        log.warn("Validation failed: {}", errors);

            return ResponseEntity
            .badRequest()
            .body(Map.of(
                    "error", "Validation failed",
                    "details", errors,
                    "timestamp", Instant.now().toString()
            ));
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {

        List<ValidationErrorMapper.ValidationError> errors = ValidationErrorMapper.from(ex);

        log.warn("Constraint violation: {}", errors);

        return ResponseEntity
                .badRequest()
                .body(Map.of(
                        "error", "Validation failed",
                        "details", errors,
                        "timestamp", Instant.now().toString()
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

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(ResourceNotFoundException ex) {
    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(Map.of(
                    "error", "NOT_FOUND",
                    "message", ex.getMessage()
            ));
    }
}
