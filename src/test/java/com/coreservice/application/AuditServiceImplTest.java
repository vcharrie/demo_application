package com.coreservice.application;

import com.coreservice.domain.Account;
import com.coreservice.infrastructure.entity.AuditEventEntity;
import com.coreservice.infrastructure.repository.AuditRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    @Mock
    AuditRepository auditRepository;

    @InjectMocks
    AuditServiceImpl auditService;

    @Test
    void shouldRecordAccountCreation() {
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        Account account = new Account(id, ownerId, BigDecimal.TEN);

        ArgumentCaptor<AuditEventEntity> captor = ArgumentCaptor.forClass(AuditEventEntity.class);

        auditService.recordAccountCreation(account);

        verify(auditRepository).save(captor.capture());
        AuditEventEntity event = captor.getValue();

        assertThat(event.getType()).isEqualTo("ACCOUNT_CREATED");
        assertThat(event.getTimestamp()).isNotNull();
    }

    @Test
    void shouldThrowIfAccountIsNull() {
        assertThatThrownBy(() -> auditService.recordAccountCreation(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account cannot be null");
    }
}