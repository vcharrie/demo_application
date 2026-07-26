package com.coreservice.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.coreservice.application.exception.TechnicalException;
import com.coreservice.config.TransferProperties;
import com.coreservice.domain.Account;
import com.coreservice.domain.AccountStatus;
import com.coreservice.domain.OperationStatus;
import com.coreservice.domain.Transfer;
import com.coreservice.domain.exception.BusinessException;
import com.coreservice.infrastructure.repository.OperationRepository;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @Mock
    private AccountService accountService;

    @Mock
    private OperationRepository operationRepository;

    private TransferServiceImpl service;

    @BeforeEach
    void setup() {
        TransferProperties props = new TransferProperties();
        props.setSeuilSensibilite(new BigDecimal("1000"));

        service = new TransferServiceImpl(
            accountService,
            null,
            props,
            operationRepository
        );
    }

    @Test
    void shouldCompleteTransferSuccessfully() {
        // Arrange
        UUID srcOwnerId = UUID.randomUUID();
        UUID srcAccountId = UUID.randomUUID();
        UUID dstOwnerId = UUID.randomUUID();
        UUID dstAccountId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100");

        Account srcAccount = new Account(srcOwnerId, srcAccountId, new BigDecimal("1000"), AccountStatus.ACTIVE);
        when(accountService.getAccount(srcAccountId)).thenReturn(srcAccount);
        
        
        Account dstAccount = new Account(dstOwnerId, dstAccountId, new BigDecimal("0"), AccountStatus.ACTIVE);
        when(accountService.getAccount(dstAccountId)).thenReturn(dstAccount);

        when(operationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Transfer result = service.initiateTransfer(srcAccountId, dstAccountId, amount);

        // Assert
        assertEquals(OperationStatus.COMPLETED, result.getStatus());
    }

    @Test
    void shouldFailWhenWithdrawFails() {
        UUID srcAccountId = UUID.randomUUID();
        UUID dstAccountId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("2000");

        Account srcAccount = new Account(UUID.randomUUID(), srcAccountId, new BigDecimal("1000"), AccountStatus.ACTIVE);
        when(accountService.getAccount(srcAccountId)).thenReturn(srcAccount);

        Account dstAccount = new Account(UUID.randomUUID(), dstAccountId, new BigDecimal("0"), AccountStatus.ACTIVE);
        when(accountService.getAccount(dstAccountId)).thenReturn(dstAccount);

        when(operationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> service.initiateTransfer(srcAccountId, dstAccountId, amount))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldFailWhenPersistenceFails() {
        UUID srcAccountId = UUID.randomUUID();
        UUID dstAccountId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("500");

        Account srcAccount = new Account(UUID.randomUUID(), srcAccountId, new BigDecimal("1000"), AccountStatus.ACTIVE);
        when(accountService.getAccount(srcAccountId)).thenReturn(srcAccount);

        Account dstAccount = new Account(UUID.randomUUID(), dstAccountId, new BigDecimal("0"), AccountStatus.ACTIVE);
        when(accountService.getAccount(dstAccountId)).thenReturn(dstAccount);
   
        when(operationRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> service.initiateTransfer(srcAccountId, dstAccountId, amount))
                .isInstanceOf(TechnicalException.class);
    }
}