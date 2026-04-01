package com.coreservice.logging;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LoggingSecurityRulesTest {

    @Test
    void shouldRedactSensitiveValue() {
        String result = LoggingSecurityRules.sanitize("password", "mySecret");
        assertEquals("***REDACTED***", result);
    }

    @Test
    void shouldNotRedactNonSensitiveValue() {
        String result = LoggingSecurityRules.sanitize("username", "john");
        assertEquals("john", result);
    }

    @Test
    void shouldSanitizeMap() {
        Map<String, String> input = Map.of(
                "password", "abc",
                "username", "john"
        );

        Map<String, String> sanitized = LoggingSecurityRules.sanitizeMap(input);

        assertEquals("***REDACTED***", sanitized.get("password"));
        assertEquals("john", sanitized.get("username"));
    }
}
