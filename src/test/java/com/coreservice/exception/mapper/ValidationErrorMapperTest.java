package com.coreservice.exception.mapper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationErrorMapperTest {

    @Test
    void shouldMapMethodArgumentNotValidException() {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "objectName");

        bindingResult.addError(new FieldError("objectName", "email", "must be a valid email"));
        bindingResult.addError(new FieldError("objectName", "age", "must be >= 18"));

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, bindingResult);

        List<ValidationErrorMapper.ValidationError> errors = ValidationErrorMapper.from(ex);

        assertThat(errors).hasSize(2);
        assertThat(errors.get(0).field()).isEqualTo("email");
        assertThat(errors.get(0).message()).isEqualTo("must be a valid email");
    }

    @Test
    void shouldMapConstraintViolationException() {
        ConstraintViolation<?> violation = Mockito.mock(ConstraintViolation.class);
        Path path = Mockito.mock(Path.class);

        Mockito.when(path.toString()).thenReturn("createUser.email");
        Mockito.when(violation.getPropertyPath()).thenReturn(path);
        Mockito.when(violation.getMessage()).thenReturn("must be a valid email");

        ConstraintViolationException ex =
                new ConstraintViolationException(Set.of(violation));

        List<ValidationErrorMapper.ValidationError> errors = ValidationErrorMapper.from(ex);

        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).field()).isEqualTo("email");
        assertThat(errors.get(0).message()).isEqualTo("must be a valid email");
    }
}
