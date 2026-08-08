package com.coreservice.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.coreservice.application.exception.FunctionalError;
import com.coreservice.application.exception.FunctionalException;
import com.coreservice.domain.Account;
import com.coreservice.domain.AccountStatus;
import com.coreservice.domain.exception.BusinessError;
import com.coreservice.domain.exception.BusinessException;
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
        assertThat(result.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void shouldThrowIfOwnerIdIsNull() {
        assertThatThrownBy(() -> accountService.createAccount(null, BigDecimal.TEN))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("owner ID");
    }

    @Test
    void shouldThrowIfInitialBalanceIsNull() {
        UUID ownerId = UUID.randomUUID();

        assertThatThrownBy(() -> accountService.createAccount(ownerId, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Amount null");
    }

    @Test
    void shouldThrowIfInitialBalanceIsNegative() {
        UUID ownerId = UUID.randomUUID();

        assertThatThrownBy(() -> accountService.createAccount(ownerId, BigDecimal.valueOf(-1)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Amount -1 is invalid");
    }

    @Test
    void shouldThrowIfAccountAlreadyExists() {
        UUID ownerId = UUID.randomUUID();
        BigDecimal initialBalance = BigDecimal.TEN;

        when(accountRepository.existsByOwnerId(ownerId)).thenReturn(true);

        assertThatThrownBy(() -> accountService.createAccount(ownerId, initialBalance))
                .isInstanceOf(FunctionalException.class)
                .hasMessageContaining("Account already exists");
    }

    @Test
    void shouldThrowIfIdIsNull() {
        assertThatThrownBy(() -> accountService.getAccount(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("id is marked non-null but is null");
    }

    @Test
    void shouldThrowIfAccountNotFound() {
        UUID id = UUID.randomUUID();

        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount(id))
                .isInstanceOf(FunctionalException.class);
    }

    @Test
    void shouldReturnAccountWhenFound() {
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        BigDecimal balance = BigDecimal.TEN;
        AccountStatus status = AccountStatus.ACTIVE;

        AccountEntity entity = new AccountEntity(ownerId, balance, status);


        when(accountRepository.findById(id)).thenReturn(Optional.of(entity));

        Account account = accountService.getAccount(id);

        assertThat(account.getOwnerId()).isEqualTo(ownerId);
        assertThat(account.getBalance()).isEqualTo(balance);
    }

    @Test
    void shouldReturnEmptyListWhenNoAccountsExist() {
        when(accountRepository.findAll()).thenReturn(List.of());

        List<Account> accounts = accountService.getAccounts();

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

        AccountEntity e1 = new AccountEntity(owner1, BigDecimal.TEN, status1);
        AccountEntity e2 = new AccountEntity(owner2, BigDecimal.ONE, status2);

        when(accountRepository.findAll()).thenReturn(List.of(e1, e2));

        List<Account> accounts = accountService.getAccounts();

        assertThat(accounts).hasSize(2);

        // Vérifie le mapping du premier
        assertThat(accounts.get(0).getOwnerId()).isEqualTo(owner1);
        assertThat(accounts.get(0).getBalance()).isEqualTo(BigDecimal.TEN);

        // Vérifie le mapping du second
        assertThat(accounts.get(1).getOwnerId()).isEqualTo(owner2);
        assertThat(accounts.get(1).getBalance()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    void shouldThrowIfIdIsNullInDelete() {
        assertThatThrownBy(() -> accountService.delete(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowIfAccountDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(accountRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> accountService.delete(id))
                .isInstanceOf(FunctionalException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldDeleteAccountWhenExists() {
        UUID id = UUID.randomUUID();

        when(accountRepository.existsById(id)).thenReturn(true);

        accountService.delete(id);

        verify(accountRepository).deleteById(id);
    }

    @Test
    void depositShouldThrowFunctionalExceptionWhenAccountNotFound() {
        UUID id = UUID.randomUUID();
        BigDecimal amount = BigDecimal.TEN;

        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                FunctionalException.class,
                () -> accountService.deposit(id, amount)
        );
    }

    @Test
    void depositShouldCreditAccountAndPersistAndAudit() {
        UUID id = UUID.randomUUID();
        BigDecimal amount = BigDecimal.TEN;

        AccountEntity entity = new AccountEntity(  
            UUID.randomUUID(),
            BigDecimal.ZERO,
            AccountStatus.ACTIVE
        );

        when(accountRepository.findById(id)).thenReturn(Optional.of(entity));

        // On capture l'entité sauvegardée
        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);

        Account result = accountService.deposit(id, amount);

        // Vérifie audit
        verify(auditService).recordCredit(any(Account.class), eq(amount));

        // Vérifie persistance
        verify(accountRepository).save(captor.capture());

        AccountEntity saved = captor.getValue();
        assertEquals(new BigDecimal("10"), saved.getBalance());

        // Vérifie retour
        assertEquals(new BigDecimal("10"), result.getBalance());
    }

    @Test
    void withdrawShouldThrowWhenAccountNotFound() {
        UUID id = UUID.randomUUID();

        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        FunctionalException ex = assertThrows(
                FunctionalException.class,
                () -> accountService.withdraw(id, new BigDecimal("10"))
        );

        assertEquals(FunctionalError.ACCOUNT_NOT_FOUND, ex.getError());
    }

    @Test
    void withdrawShouldCallDomainAndPersistAndAudit() {
        UUID id = UUID.randomUUID();

        AccountEntity entity = new AccountEntity(
                id,
                new BigDecimal("100.00"),
                AccountStatus.ACTIVE
        );

        when(accountRepository.findById(id)).thenReturn(Optional.of(entity));

        // Exécution
        accountService.withdraw(id, new BigDecimal("30"));

        // Vérification audit
        verify(auditService, times(1))
                .recordDebit(any(Account.class), eq(new BigDecimal("30")));

        // Vérification persistance
        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountRepository, times(1)).save(captor.capture());

        AccountEntity saved = captor.getValue();
        assertEquals(new BigDecimal("70.00"), saved.getBalance());
        assertEquals(AccountStatus.ACTIVE, saved.getStatus());
    }

    @Test
    void withdrawShouldPropagateBusinessExceptionsFromDomain() {
        UUID id = UUID.randomUUID();

        AccountEntity entity = new AccountEntity(  UUID.randomUUID(),     
                new BigDecimal("10.00"),
                AccountStatus.ACTIVE
        );

        when(accountRepository.findById(id)).thenReturn(Optional.of(entity));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> accountService.withdraw(id, new BigDecimal("50"))
        );

        assertEquals(BusinessError.ACCOUNT_INSUFFICIENT_FUNDS, ex.getError());
    }

}