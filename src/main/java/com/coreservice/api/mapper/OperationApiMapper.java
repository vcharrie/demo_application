package com.coreservice.api.mapper;

import com.coreservice.api.dto.OperationStatus;
import com.coreservice.api.dto.OperationType;
import com.coreservice.domain.Operation;
import com.coreservice.domain.Transfer;

public class OperationApiMapper {

    public static com.coreservice.api.dto.Operation toResponse(Operation operation) {
        com.coreservice.api.dto.Operation reponse = null;
        if (operation instanceof Transfer) {
            Transfer transfer = (Transfer) operation;
            OperationType operationType = OperationType.valueOf(operation.getType().name());
            OperationStatus operationStatus = OperationStatus.valueOf(operation.getStatus().name());

            reponse = new com.coreservice.api.dto.Operation(transfer.getId(),
                                                            operationType,
                                                            operationStatus,
                                                            transfer.getSourceAccountId(),
                                                            transfer.getDestinationAccountId(),
                                                            transfer.getAmount());
        }
        
        return reponse;
    }

}
