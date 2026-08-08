package com.coreservice.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        UUID ownerId,
        BigDecimal balance
) { }