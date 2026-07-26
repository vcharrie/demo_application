package com.coreservice.api.mapper;

import com.coreservice.api.dto.OperationStatus;
import com.coreservice.api.dto.TransferResult;
import com.coreservice.domain.Transfer;

public class TransferApiMapper {
    public static TransferResult toTransferResult(Transfer op) {
        OperationStatus status = null;
        
        switch (op.getStatus()) {
            case PENDING -> status = OperationStatus.PENDING;
            case COMPLETED -> status = OperationStatus.COMPLETED;
            case FAILED -> status = OperationStatus.FAILED;
        };
                
        return new TransferResult(
                status,
                op.getSourceAccountId(),
                op.getDestinationAccountId(),
                op.getAmount()
        );
    }
}