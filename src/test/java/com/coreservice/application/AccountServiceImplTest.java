package com.coreservice.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.coreservice.domain.Account;
import com.coreservice.domain.AccountStatus;
import com.coreservice.domain.AuditService;
import com.coreservice.domain.exception.ConflictException;
import com.coreservice.domain.exception.ResourceNotFoundException;
import com.coreservice.infrastructure.entity.AccountEntity;
import com.coreservice.infrastructure.repository.AccountRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    AuditService auditService;

    @Mock
    AccountRepository accountRepository;

    @InjectMocks
    AccountServiceImpl accountService;

    @Test
    void shouldCreateAccountSuccessfully() {
        UUID ownerId = UUID.randomUUID();
        BigDecimal initialBalance = BigDecimal.TEN;

        // Le repository ne doit pas signaler de conflit
        when(accountRepository.existsByOwnerId(ownerId)).thenReturn(false);



        Account result = accountService.createAccount(ownerId, initialBalance);

        // Vérifie que le repository a bien été appelé
        verify(accountRepository).save(any(AccountEntity.class));

        // Vérifie le domaine retourné
        assertThat(result.getOwnerId()).isEqualTo(ownerId);
        assertThat(result.getBalance()).isEqualTo(initialBalance);
        assertThat(result.getId()).isNotNull(); // UUID.randomUUID()
    }

    @Test
    void shouldThrowIfOwnerIdIsNull() {
        assertThatThrownBy(() -> accountService.createAccount(null, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Owner ID");
    }

    @Test
    void shouldThrowIfInitialBalanceIsNull() {
        UUID ownerId = UUID.randomUUID();

        assertThatThrownBy(() -> accountService.createAccount(ownerId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Initial balance");
    }

    @Test
    void shouldThrowIfInitialBalanceIsNegative() {
        UUID ownerId = UUID.randomUUID();

        assertThatThrownBy(() -> accountService.createAccount(ownerId, BigDecimal.valueOf(-1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Initial balance");
    }

    @Test
    void shouldThrowIfAccountAlreadyExists() {
        UUID ownerId = UUID.randomUUID();
        BigDecimal initialBalance = BigDecimal.TEN;

        when(accountRepository.existsByOwnerId(ownerId)).thenReturn(true);

        assertThatThrownBy(() -> accountService.createAccount(ownerId, initialBalance))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Account already exists");
    }

    @Test
    void shouldThrowIfIdIsNull() {
        assertThatThrownBy(() -> accountService.findById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account ID cannot be null");
    }

    @Test
    void shouldThrowIfAccountNotFound() {
        UUID id = UUID.randomUUID();

        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Account not found");
    }

    @Test
    void shouldReturnAccountWhenFound() {
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        BigDecimal balance = BigDecimal.TEN;
        AccountStatus status = AccountStatus.ACTIVE;

        AccountEntity entity = new AccountEntity(id, ownerId, balance, status);

        when(accountRepository.findById(id)).thenReturn(Optional.of(entity));

        Account account = accountService.findById(id);

        assertThat(account.getId()).isEqualTo(id);
        assertThat(account.getOwnerId()).isEqualTo(ownerId);
        assertThat(account.getBalance()).isEqualTo(balance);
    }

    @Test
    void shouldReturnEmptyListWhenNoAccountsExist() {
        when(accountRepository.findAll()).thenReturn(List.of());

        List<Account> accounts = accountService.findAll();

        assertThat(accounts).isEmpty();
    }

    @Test
    void shouldReturnAllAccounts() {
        UUID id1 = UUID.randomUUID();
        UUID owner1 = UUID.randomUUID();
        AccountStatus status1 = AccountStatus.ACTIVE;
        UUID id2 = UUID.randomUUID();
        UUID owner2 = UUID.randomUUID();
        AccountStatus status2 = AccountStatus.ACTIVE;

        AccountEntity e1 = new AccountEntity(id1, owner1, BigDecimal.TEN, status1);
        AccountEntity e2 = new AccountEntity(id2, owner2, BigDecimal.ONE, status2);

        when(accountRepository.findAll()).thenReturn(List.of(e1, e2));

        List<Account> accounts = accountService.findAll();

        assertThat(accounts).hasSize(2);

        // Vérifie le mapping du premier
        assertThat(accounts.get(0).getId()).isEqualTo(id1);
        assertThat(accounts.get(0).getOwnerId()).isEqualTo(owner1);
        assertThat(accounts.get(0).getBalance()).isEqualTo(BigDecimal.TEN);

        // Vérifie le mapping du second
        assertThat(accounts.get(1).getId()).isEqualTo(id2);
        assertThat(accounts.get(1).getOwnerId()).isEqualTo(owner2);
        assertThat(accounts.get(1).getBalance()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    void shouldThrowIfIdIsNullInDelete() {
        assertThatThrownBy(() -> accountService.delete(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account ID cannot be null");
    }

    @Test
    void shouldThrowIfAccountDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(accountRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> accountService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Account not found");
    }

    @Test
    void shouldDeleteAccountWhenExists() {
        UUID id = UUID.randomUUID();

        when(accountRepository.existsById(id)).thenReturn(true);

        accountService.delete(id);

        verify(accountRepository).deleteById(id);
    }

}