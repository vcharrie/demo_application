package com.coreservice.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferResult(
    OperationStatus status,
    UUID sourceAccountId,
    UUID destinationAccountId,
    BigDecimal amount
) {    
}
