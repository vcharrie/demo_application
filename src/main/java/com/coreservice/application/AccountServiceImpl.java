package com.coreservice.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.coreservice.application.exception.FunctionalError;
import com.coreservice.application.exception.FunctionalException;
import com.coreservice.domain.Account;
import com.coreservice.infrastructure.entity.AccountEntity;
import com.coreservice.infrastructure.mapper.AccountMapper;
import com.coreservice.infrastructure.repository.AccountRepository;

import jakarta.transaction.Transactional;
import lombok.NonNull;


@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AuditService auditService;

    public AccountServiceImpl(AccountRepository accountRepository,
                          AuditService auditService) {
        this.accountRepository = accountRepository;
        this.auditService = auditService;
    }

    @Transactional // SEC-TXN-01
    public Account createAccount(UUID ownerId, BigDecimal initialBalance) {

        // Domain model
        Account domainAccount = new Account(
                ownerId,
                initialBalance
        );

        // CA-01.2 — titulaire existe déjà
        if (accountRepository.existsByOwnerId(ownerId)) {
            throw new FunctionalException(FunctionalError.ACCOUNT_ALREADY_EXISTS, ownerId.toString());
        }

        // Mapping domain → entity
        AccountEntity entity = AccountMapper.toEntity(domainAccount);

        accountRepository.save(entity);

        // CA-01.4 — Historisation obligatoire → SEC-AUDIT-01
        auditService.recordAccountCreation(domainAccount);

        return AccountMapper.toDomain(entity);
    }

    @Override
    public void delete(@NonNull UUID id) {

        if (!accountRepository.existsById(id)) {
            throw new FunctionalException(FunctionalError.ACCOUNT_NOT_FOUND, id.toString());
        }

        accountRepository.deleteById(id);

    }

    @Override
    public List<Account> findAll() {
        return accountRepository.findAll()
            .stream()
            .map(AccountMapper::toDomain)
            .toList();
    }

    @Override
    public Account findById(@NonNull UUID id) {

        AccountEntity entity = accountRepository.findById(id)
            .orElseThrow(() -> new FunctionalException(FunctionalError.ACCOUNT_NOT_FOUND, id.toString()));

        return AccountMapper.toDomain(entity);
    }

    @Transactional
    @Override
    public Account deposit(@NonNull UUID accountId, @NonNull BigDecimal amount) {

        // Récupération du compte
        AccountEntity entity = accountRepository.findById(accountId)
                .orElseThrow(() -> new FunctionalException(FunctionalError.ACCOUNT_NOT_FOUND, accountId.toString()));

        Account account = AccountMapper.toDomain(entity);

        // Règle métier (domaine)
        account.credit(amount); // peut lever BusinessException

        // Historisation (audit)
        auditService.recordCredit(account, amount);

        // Persistance
        AccountEntity updated = AccountMapper.toEntity(account);
        accountRepository.save(updated);

        return account;
    }
    

}
