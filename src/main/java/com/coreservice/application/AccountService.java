package com.coreservice.application;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.coreservice.domain.Account;
import com.coreservice.domain.exception.ConflictException;
import com.coreservice.infrastructure.entity.AccountEntity;
import com.coreservice.infrastructure.mapper.AccountMapper;
import com.coreservice.infrastructure.repository.AccountRepository;

import jakarta.transaction.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    //private final AuditService auditService;

    public AccountService(AccountRepository accountRepository,
                          AuditService auditService) {
        this.accountRepository = accountRepository;
        //this.auditService = auditService;
    }

    @Transactional // SEC-TXN-01
    public Account createAccount(UUID ownerId, BigDecimal initialBalance) {

        // CA-01.1 — données invalides → SEC-VAL-01
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID cannot be null");
        }
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance must be >= 0");
        }

        // CA-01.2 — titulaire existe déjà
        if (accountRepository.existsByOwnerId(ownerId)) {
            throw new ConflictException("Account already exists for this owner");
        }

        // Domain model
        Account domainAccount = new Account(
                UUID.randomUUID(),
                ownerId,
                initialBalance
        );

        // Mapping domain → entity
        AccountEntity entity = AccountMapper.toEntity(domainAccount);

        accountRepository.save(entity);

        // CA-01.4 — Historisation obligatoire → SEC-AUDIT-01
        //auditService.recordAccountCreation(domainAccount);

        return domainAccount;
    }
}
