package com.coreservice.domain;

import java.math.BigDecimal;
import java.util.UUID;

import com.coreservice.domain.exception.BusinessError;
import com.coreservice.domain.exception.BusinessException;

public class Account {

    private UUID id;
    private final UUID ownerId;
    private BigDecimal balance;
    private AccountStatus status;

    public Account(UUID id, UUID ownerId, BigDecimal initialBalance, AccountStatus status) {
        this.id = id;
        this.ownerId = ownerId;
        this.balance = initialBalance;
        this.status = status != null ? status : AccountStatus.ACTIVE;
    }

    public Account(UUID ownerId, BigDecimal initialBalance) {

        if (ownerId == null) {
            throw new BusinessException(BusinessError.ACCOUNT_OWNER_ID_NULL);
        }
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(BusinessError.AMOUNT_INVALID, initialBalance);
        }

        this.ownerId = ownerId;
        this.balance = initialBalance;
        this.status = AccountStatus.ACTIVE;
    }

    // --- Business rules ---

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public boolean isSuspended() {
        return status == AccountStatus.SUSPENDED;
    }

    public boolean isClosed() {
        return status == AccountStatus.CLOSED;
    }

    public void credit(BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(BusinessError.AMOUNT_INVALID, amount);
        }

        if (this.isSuspended()) {
            throw new BusinessException(BusinessError.ACCOUNT_SUSPENDED, this.id.toString());
        }

        this.balance = this.balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(BusinessError.AMOUNT_INVALID, amount);
        }

        if (this.isSuspended()) {
            throw new BusinessException(BusinessError.ACCOUNT_SUSPENDED, this.id.toString());
        } 

        if (this.balance.compareTo(amount) < 0) {
            throw new BusinessException(BusinessError.ACCOUNT_INSUFFICIENT_FUNDS, this.balance, amount);
        }

        this.balance = this.balance.subtract(amount);
    }

    // --- Getters ---

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    // --- Status transitions (if needed later) ---

    public void suspend() {
        this.status = AccountStatus.SUSPENDED;
    }

    public void close() {
        this.status = AccountStatus.CLOSED;
    }

    public void activate() {
        this.status = AccountStatus.ACTIVE;
    }
}