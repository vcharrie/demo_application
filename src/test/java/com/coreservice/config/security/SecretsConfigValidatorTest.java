package com.coreservice.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "app.security.token=test-token",
        "app.security.salt=test-salt"
})
class SecretsConfigValidatorTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads_whenSecretsArePresent() {
        assertNotNull(context);
    }

    /**
     * Petite application Spring Boot minimale pour tester le démarrage.
     * Cela évite de lancer toute ton application principale.
     */
    @SpringBootTest
    static class TestApplication { }
}
