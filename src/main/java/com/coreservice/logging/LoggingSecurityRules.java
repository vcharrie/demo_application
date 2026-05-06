package com.coreservice.logging;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class LoggingSecurityRules {

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password",
            "token",
            "secret",
            "salt",
            "apikey",
            "authorization"
    );

    private static final String REDACTED = "***REDACTED***";

    private LoggingSecurityRules() {
        // Utility class
    }

    public static String sanitize(String key, String value) {
        if (key == null || value == null) {
            return value;
        }

        String lowerKey = key.toLowerCase(Locale.ROOT);

        boolean isSensitive = SENSITIVE_KEYS.stream()
                .anyMatch(lowerKey::contains);

        return isSensitive ? REDACTED : value;
    }

    public static Map<String, String> sanitizeMap(Map<String, String> input) {
        if (input == null) {
            return null;
        }

        return input.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> sanitize(e.getKey(), e.getValue())
                ));
    }
}

