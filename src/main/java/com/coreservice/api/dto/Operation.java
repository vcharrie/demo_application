package com.coreservice.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record Operation(
        UUID id,
        OperationType type,
        OperationStatus status,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount
) { }