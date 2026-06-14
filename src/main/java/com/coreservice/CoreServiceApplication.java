package com.coreservice;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CoreServiceApplication {

    private final Environment env;

    @Autowired
    ApplicationContext ctx;

    @Value("${app.security.disabled:false}")
    boolean securityDisabled;

    public CoreServiceApplication(Environment env) {
        this.env = env;
    }

    

    @PostConstruct
    public void debugBeans() {
        System.out.println(">>> SecurityFilterChain beans = " +
            Arrays.toString(ctx.getBeanNamesForType(org.springframework.security.web.SecurityFilterChain.class)));
    }

    @PostConstruct
    public void debugProfiles() {
        System.out.println(">>> ACTIVE PROFILES = " + Arrays.toString(env.getActiveProfiles()));
        System.out.println(">>> app.security.disabled = " + securityDisabled);
    }

    public static void main(String[] args) {
        SpringApplication.run(CoreServiceApplication.class, args);
    }
}
