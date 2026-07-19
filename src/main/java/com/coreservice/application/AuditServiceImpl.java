package com.coreservice.application;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coreservice.domain.Account;
import com.coreservice.domain.AuditEvent;
import com.coreservice.infrastructure.entity.AuditEventEntity;
import com.coreservice.infrastructure.mapper.AuditMapper;
import com.coreservice.infrastructure.repository.AuditRepository;

@Service
public class AuditServiceImpl implements AuditService {

    private final AuditRepository auditRepository;

    public AuditServiceImpl(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Override
    @Transactional
    public void recordAccountCreation(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }

        AuditEvent event = new AuditEvent(
                "ACCOUNT_CREATED",
                "Account " + account.getId() + " created for owner " + account.getOwnerId()
        );

        AuditEventEntity entity = AuditMapper.toEntity(event);

        auditRepository.save(entity);
    }

    @Override
    public void recordCredit(Account account, BigDecimal amount) {
        AuditEvent entry = new AuditEvent(
                "ACCOUNT_CREDITED",
                "Credit of " + amount + " applied to account" + account.getId() + " at " + Instant.now()
        );

        AuditEventEntity entity = AuditMapper.toEntity(entry);

        auditRepository.save(entity);
    }

}   