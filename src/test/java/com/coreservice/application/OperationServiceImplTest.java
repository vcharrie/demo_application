package com.coreservice.application;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.coreservice.api.dto.Operation;
import com.coreservice.application.exception.FunctionalException;
import com.coreservice.domain.AccountStatus;

import com.coreservice.infrastructure.entity.AccountEntity;
import com.coreservice.infrastructure.entity.OperationEntity;
import com.coreservice.infrastructure.repository.AccountRepository;
import com.coreservice.infrastructure.repository.OperationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OperationServiceImplTest {

    private AccountRepository accountRepository;
    private OperationRepository operationRepository;
    private OperationServiceImpl service;

    @BeforeEach
    void setup() {
        accountRepository = mock(AccountRepository.class);
        operationRepository = mock(OperationRepository.class);

        service = new OperationServiceImpl(accountRepository, operationRepository);
    }

    @Test
    void getAccountHistoryReturnsOperationsWhenAccountExists() {
        UUID accountId = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("1000.0");
        AccountStatus accountStatus = AccountStatus.ACTIVE;

        AccountEntity accountEntity = new AccountEntity(accountId, balance, accountStatus);

        // Compte existant
        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(accountEntity));

        // Deux opérations (source et destination)
        OperationEntity op1 = new OperationEntity(
                com.coreservice.infrastructure.entity.OperationType.TRANSFER,
                com.coreservice.infrastructure.entity.OperationStatus.COMPLETED,
                accountId,
                UUID.randomUUID(),
                BigDecimal.TEN
        );

        OperationEntity op2 = new OperationEntity(
                com.coreservice.infrastructure.entity.OperationType.TRANSFER,
                com.coreservice.infrastructure.entity.OperationStatus.PENDING,
                UUID.randomUUID(),
                accountId,
                BigDecimal.ONE
        );

        when(operationRepository.findBySourceAccountIdOrDestinationAccountId(accountId, accountId))
                .thenReturn(List.of(op1, op2));

        // Appel
        List<Operation> result = service.getAccountHistory(accountId).operations();

        // Vérifications
        assertThat(result).hasSize(2);
        assertThat(result.get(0).sourceAccountId()).isEqualTo(accountId);
        assertThat(result.get(1).destinationAccountId()).isEqualTo(accountId);
    }

    @Test
    void getAccountHistoryReturnsEmptyListWhenNoOperations() {
        UUID accountId = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("1000.0");
        AccountStatus accountStatus = AccountStatus.ACTIVE;

        AccountEntity accountEntity = new AccountEntity(accountId, balance, accountStatus);

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(accountEntity));

        when(operationRepository.findBySourceAccountIdOrDestinationAccountId(accountId, accountId))
                .thenReturn(List.of());

        List<Operation> result = service.getAccountHistory(accountId).operations();

        assertThat(result).isEmpty();
    }

    @Test
    void getAccountHistoryThrowsExceptionWhenAccountDoesNotExist() {
        UUID accountId = UUID.randomUUID();

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAccountHistory(accountId))
                .isInstanceOf(FunctionalException.class)
                .hasMessageContaining("not found");
    }
}
