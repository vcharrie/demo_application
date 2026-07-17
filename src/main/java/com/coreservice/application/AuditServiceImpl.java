package com.coreservice.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coreservice.domain.Account;
import com.coreservice.domain.AuditEvent;
import com.coreservice.domain.AuditService;
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
}   