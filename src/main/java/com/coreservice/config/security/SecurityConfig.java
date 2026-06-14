package com.coreservice.config.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.annotation.PostConstruct;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableConfigurationProperties(SecurityProperties.class)
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SecurityProperties props;

    public SecurityConfig(SecurityProperties props) {
        this.props = props;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean(name = "securityFilterChain")
    @Order(0)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        System.out.println(">>> SecurityConfig.props.disabled = " + props.disabled());

        if (props.disabled()) {
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .securityContext(security -> security.disable())
                .sessionManagement(session -> session.disable())
                .requestCache(cache -> cache.disable())
                .anonymous(anon -> anon.disable())
                .logout(logout -> logout.disable())
                .exceptionHandling(ex -> ex.disable());

            System.out.println(">>> dans props.disabled() ");    

            return http.build();
        }
        
        http
            .csrf(csrf -> csrf.disable())

            // Basic Auth, version lambda (pas de withDefaults())
            .httpBasic(httpBasic -> { })

            .authorizeHttpRequests(auth -> auth
               // 👉 Healthcheck accessible sans authentification
              .requestMatchers("/actuator/health").permitAll()

              // 👉 Tout le reste nécessite Basic Auth
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

    @PostConstruct
    public void debugProps() {
        System.out.println(">>> SecurityConfig.props.disabled = " + props.disabled());
    }

}
