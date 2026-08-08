package com.coreservice.application;

import java.util.UUID;

import com.coreservice.api.dto.AccountHistoryResponse;
import com.coreservice.domain.exception.BusinessException;


public interface OperationService {

    /**
     * Retourne l'historique des opérations d'un compte.
     *
     * @param accountId identifiant du compte
     * @return liste des opérations (vide si aucune)
     * @throws BusinessException si le compte n'existe pas
     */
    AccountHistoryResponse getAccountHistory(UUID accountId);
}