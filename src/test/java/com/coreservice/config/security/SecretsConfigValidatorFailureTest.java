package com.coreservice.config.security;

import com.coreservice.CoreServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecretsConfigValidatorFailureTest {

    @Test
    void contextFails_whenSecretIsMissing() {
        BeanCreationException ex = assertThrows(BeanCreationException.class, () -> {
            SpringApplication app = new SpringApplication(CoreServiceApplication.class);
            app.run(
                "--app.security.token=",
                "--app.security.salt=test-salt"
            );
    });

    assertTrue(ex.getCause() instanceof IllegalStateException);
    }
}
