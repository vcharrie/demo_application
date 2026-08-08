package com.coreservice.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coreservice.api.dto.AccountHistoryResponse;
import com.coreservice.api.mapper.OperationApiMapper;
import com.coreservice.application.exception.FunctionalError;
import com.coreservice.application.exception.FunctionalException;
import com.coreservice.domain.Operation;
import com.coreservice.infrastructure.entity.AccountEntity;
import com.coreservice.infrastructure.mapper.OperationMapper;
import com.coreservice.infrastructure.repository.AccountRepository;
import com.coreservice.infrastructure.repository.OperationRepository;

@Service
public class OperationServiceImpl implements OperationService {

    private final AccountRepository accountRepository;
    private final OperationRepository operationRepository;

    public OperationServiceImpl(AccountRepository accountRepository,
                                OperationRepository operationRepository) {
        this.accountRepository = accountRepository;
        this.operationRepository = operationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AccountHistoryResponse getAccountHistory(UUID accountId) {

        // CA-06.3 : Si le compte n’existe pas → erreur
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new FunctionalException(
                        FunctionalError.ACCOUNT_NOT_FOUND,
                        accountId.toString()
                ));

        // CA-06.1 + CA-06.2 : Retourner les opérations (liste vide si aucune)
        List<Operation> listOperations = operationRepository.findBySourceAccountIdOrDestinationAccountId(accountId, accountId)
                .stream()
                .map(OperationMapper::toDomain)
                .toList();

        List<com.coreservice.api.dto.Operation> listOps = listOperations
                    .stream()
                    .map(OperationApiMapper::toResponse)
                    .toList();

        return new AccountHistoryResponse(accountId, listOps);
    }
}