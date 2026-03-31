package com.coreservice.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private String token;
    private String salt;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }
}

