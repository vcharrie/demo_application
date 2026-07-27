package com.coreservice.application;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.coreservice.domain.Transfer;
import com.coreservice.domain.ValidationDecision;

@Service
public interface TransferService {

    /**
     * Initie un virement interne entre deux comptes.
     *
     * Règles métier (US-04) :
     * - montant > 0
     * - compte source != compte destination
     * - compte source non SUSPENDED
     * - solde suffisant
     * - compte destination non CLOSED
     * - statut COMPLETED si montant <= seuil
     * - statut PENDING si montant > seuil
     * - historisation obligatoire
     */
    Transfer initiateTransfer(UUID sourceAccountId, UUID destinationAccountId, BigDecimal amount);

    /**
     * Valide un virement en attente.
     *
     * @param transferId l'identifiant du virement
     * @param decision la décision de validation
     */
    public void validateTransfer(UUID transferId, ValidationDecision decision);
}