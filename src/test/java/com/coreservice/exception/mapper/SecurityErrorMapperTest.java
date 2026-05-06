package com.coreservice.exception.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityErrorMapperTest {

    @Test
    void shouldMapAuthenticationException() {
        AuthenticationException ex = new AuthenticationException("Invalid token") {};

        var error = SecurityErrorMapper.from(ex);

        assertThat(error.error()).isEqualTo("UNAUTHORIZED");
        assertThat(error.message()).isEqualTo("Invalid token");
    }

    @Test
    void shouldMapAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Forbidden area");

        var error = SecurityErrorMapper.from(ex);

        assertThat(error.error()).isEqualTo("FORBIDDEN");
        assertThat(error.message()).isEqualTo("Forbidden area");
    }
}
