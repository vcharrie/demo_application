package com.coreservice.config.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.InitializingBean;

@Component
@EnableConfigurationProperties(SecurityProperties.class)
public class SecretsConfigValidator implements InitializingBean {

    private final SecurityProperties securityProperties;

    public SecretsConfigValidator(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    public void afterPropertiesSet() {
        validateNotEmpty("token", securityProperties.getToken());
        validateNotEmpty("salt", securityProperties.getSalt());
        // Ajoute ici d'autres secrets si nécessaire
    }

    private void validateNotEmpty(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                "Missing required security configuration: app.security." + name
            );
        }
    }
}
