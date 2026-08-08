package com.coreservice.api.dto;

import java.util.UUID;

public record TransferValidationRequest(
    UUID transferId,
    ValidationDecision decision
) {    
}
