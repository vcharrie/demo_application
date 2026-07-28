package com.coreservice.api.mapper;

import com.coreservice.api.dto.OperationStatus;
import com.coreservice.api.dto.TransferResult;
import com.coreservice.api.dto.TransferValidationResult;
import com.coreservice.domain.Transfer;
import com.coreservice.domain.ValidationDecision;

public class TransferApiMapper {

    public static TransferValidationResult toTransferValidationResult(Transfer op) {
        OperationStatus status = null;
        
        switch (op.getStatus()) {
            case PENDING -> status = OperationStatus.PENDING;
            case COMPLETED -> status = OperationStatus.COMPLETED;
            case FAILED -> status = OperationStatus.FAILED;
        };
                
        return new TransferValidationResult(
                op.getId(),
                status
        );
    }

    public static TransferResult toTransferResult(Transfer op) {
        OperationStatus status = null;
        
        switch (op.getStatus()) {
            case PENDING -> status = OperationStatus.PENDING;
            case COMPLETED -> status = OperationStatus.COMPLETED;
            case FAILED -> status = OperationStatus.FAILED;
        };
                
        return new TransferResult(
                op.getId(),
                status,
                op.getSourceAccountId(),
                op.getDestinationAccountId(),
                op.getAmount()
        );
    }

    public static ValidationDecision toValidationDecision(ValidationDecision vd) {
        ValidationDecision vDecision = null;

        switch (vd) {
            case APPROVE -> vDecision = ValidationDecision.APPROVE;
            case REJECT -> vDecision = ValidationDecision.REJECT;
        }

        return vDecision;
    }

    public static ValidationDecision toValidationDecision(com.coreservice.api.dto.ValidationDecision vd) {
        ValidationDecision vDecision = null;

        switch (vd) {
            case APPROVE -> vDecision = ValidationDecision.APPROVE;
            case REJECT -> vDecision = ValidationDecision.REJECT;
        }

        return vDecision;
    }

    public static com.coreservice.api.dto.ValidationDecision toValidationDecisionDTO(ValidationDecision vd) {
        com.coreservice.api.dto.ValidationDecision vDecision = null;

        switch (vd) {
            case APPROVE -> vDecision = com.coreservice.api.dto.ValidationDecision.APPROVE;
            case REJECT -> vDecision = com.coreservice.api.dto.ValidationDecision.REJECT;
        }

        return vDecision;
    }

}