package com.coreservice.exception.mapper;

import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.util.List;
import java.util.stream.Collectors;

public final class ValidationErrorMapper {

    private ValidationErrorMapper() {
        // Utility class
    }

    public static List<ValidationError> from(MethodArgumentNotValidException ex) {
        return ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ValidationError(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());
    }

    public static List<ValidationError> from(ConstraintViolationException ex) {
        return ex.getConstraintViolations()
                .stream()
                .map(violation -> new ValidationError(
                        extractFieldName(violation),
                        violation.getMessage()
                ))
                .collect(Collectors.toList());
    }

    private static String extractFieldName(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        int lastDot = path.lastIndexOf('.');
        return lastDot != -1 ? path.substring(lastDot + 1) : path;
    }

    public record ValidationError(String field, String message) {}
}
