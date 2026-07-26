package com.coreservice.domain;

import java.math.BigDecimal;
import java.util.UUID;

public class Transfer extends Operation {

    private final UUID sourceAccountId;
    private final UUID destinationAccountId;
    private final BigDecimal amount;

    public Transfer(UUID sourceAccountId, UUID destinationAccountId, BigDecimal amount) {
        super(OperationType.TRANSFER);
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
    }

    public UUID getSourceAccountId() {
        return sourceAccountId;
    }

    public UUID getDestinationAccountId() {
        return destinationAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void markCompleted() {
        this.setStatus(OperationStatus.COMPLETED);
    }

    public void markPending() {
        this.setStatus(OperationStatus.PENDING);
    }

    public void markFailed() {
        this.setStatus(OperationStatus.FAILED);
    }
}