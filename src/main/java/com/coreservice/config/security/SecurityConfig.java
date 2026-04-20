package com.coreservice.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            // Basic Auth, version lambda (pas de withDefaults())
            .httpBasic(httpBasic -> { })

            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, authEx) -> {
                    throw authEx;
                })
                .accessDeniedHandler((req, res, accessDeniedEx) -> {
                    throw accessDeniedEx;
                })
            );

        return http.build();
    }
}
