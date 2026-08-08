package com.coreservice.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "transfer")
public class TransferProperties {

    private BigDecimal seuilSensibilite;

    public BigDecimal getSeuilSensibilite() {
        return seuilSensibilite;
    }

    public void setSeuilSensibilite(BigDecimal seuilSensibilite) {
        this.seuilSensibilite = seuilSensibilite;
    }
}
