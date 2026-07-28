package com.coreservice.api.dto;

import java.util.UUID;

public record TransferValidationResult (
    UUID transferId,
    OperationStatus status) {    
}
    
