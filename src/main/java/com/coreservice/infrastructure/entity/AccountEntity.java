package com.coreservice.infrastructure.entity;

import java.math.BigDecimal;
import java.util.UUID;

import com.coreservice.domain.AccountStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

@Entity
public class AccountEntity {

    @Id
    private UUID id;

    private UUID ownerId;

    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    public AccountEntity(UUID id, UUID ownerId, BigDecimal balance, AccountStatus status) {
        this.id = id;
        this.ownerId = ownerId;
        this.balance = balance;
        this.status = status;
    }

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

    public void setId(UUID id) {
        this.id = id;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    // getters/setters
}
