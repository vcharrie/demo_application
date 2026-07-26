package com.coreservice.infrastructure.mapper;

import com.coreservice.domain.Operation;
import com.coreservice.domain.OperationStatus;
import com.coreservice.domain.OperationType;
import com.coreservice.domain.Transfer;
import com.coreservice.infrastructure.entity.OperationEntity;


public class OperationMapper {

    // ---------------------------
    // ENTITY → DOMAIN
    // ---------------------------
    public static Operation toDomain(OperationEntity entity) {

        if (entity.getType() != com.coreservice.infrastructure.entity.OperationType.TRANSFER) {
            throw new IllegalArgumentException("Unsupported operation type: " + entity.getType());
        }

        // Reconstruire un Transfer du domaine
        Transfer transfer = new Transfer(
                entity.getSourceAccountId(),
                entity.getDestinationAccountId(),
                entity.getAmount()
        );

        // Remettre le statut
        transfer.setStatus(toDomain(entity.getStatus()));

        return transfer;
    }

    // ---------------------------
    // DOMAIN → ENTITY
    // ---------------------------
    public static OperationEntity toEntity(Operation domainOp) {
        if (domainOp instanceof Transfer transfer) {
            return new OperationEntity(
                com.coreservice.infrastructure.entity.OperationType.TRANSFER,
                toEntity(transfer.getStatus()),
                transfer.getSourceAccountId(),
                transfer.getDestinationAccountId(),
                transfer.getAmount()
        );
        } else {
            throw new IllegalArgumentException("Only Transfer operations are supported");
        }
    }

    // ---------------------------
    // ENUM MAPPING
    // ---------------------------
    public static OperationType toDomain(com.coreservice.infrastructure.entity.OperationType entityType) {
        return switch (entityType) {
            case TRANSFER -> OperationType.TRANSFER;
            default -> throw new IllegalArgumentException("Unsupported OperationType: " + entityType);
        };
    }

    public static OperationStatus toDomain(com.coreservice.infrastructure.entity.OperationStatus entityStatus) {
        return switch (entityStatus) {
            case PENDING -> OperationStatus.PENDING;
            case COMPLETED -> OperationStatus.COMPLETED;
            case FAILED -> OperationStatus.FAILED;
        };
    }

    public static com.coreservice.infrastructure.entity.OperationStatus toEntity(OperationStatus domainStatus) {
        return switch (domainStatus) {
            case PENDING -> com.coreservice.infrastructure.entity.OperationStatus.PENDING;
            case COMPLETED -> com.coreservice.infrastructure.entity.OperationStatus.COMPLETED;
            case FAILED -> com.coreservice.infrastructure.entity.OperationStatus.FAILED;
        };
    }
}