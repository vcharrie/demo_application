package com.coreservice.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AccountRequest(
        @NotNull UUID ownerId,
        @NotNull @PositiveOrZero BigDecimal initialBalance
)  { }
