package com.coreservice.application;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.coreservice.api.dto.TransferValidationRequest;
import com.coreservice.api.dto.TransferValidationResult;
import com.coreservice.api.mapper.TransferApiMapper;
import com.coreservice.application.exception.TechnicalError;
import com.coreservice.application.exception.TechnicalException;
import com.coreservice.config.TransferProperties;
import com.coreservice.domain.Account;
import com.coreservice.domain.OperationStatus;
import com.coreservice.domain.Transfer;
import com.coreservice.domain.ValidationDecision;
import com.coreservice.domain.exception.BusinessError;
import com.coreservice.domain.exception.BusinessException;
import com.coreservice.infrastructure.entity.OperationEntity;
import com.coreservice.infrastructure.mapper.OperationMapper;
import com.coreservice.infrastructure.repository.OperationRepository;

import jakarta.transaction.Transactional;
import lombok.NonNull;

@Service
public class TransferServiceImpl implements TransferService {

    private final AccountService accountService;
    private final AuditService auditService;
    private final BigDecimal seuilSensibilite;
    private final OperationRepository operationRepository;
    

    public TransferServiceImpl(AccountService accountService,
                               AuditService auditService,
                               TransferProperties transferProperties,
                               OperationRepository operationRepository) {
        this.accountService = accountService;
        this.auditService = auditService;
        this.seuilSensibilite = transferProperties.getSeuilSensibilite();
        this.operationRepository = operationRepository;
    }

    private void persistSafely(Transfer transfer) {
        try {
            OperationEntity entity = OperationMapper.toEntity(transfer);
            operationRepository.save(entity);
        } catch (Exception e) {
            throw new TechnicalException(TechnicalError.TRANSFER_FAILED, e);
        }
    }

    @Override
    @Transactional
    public Transfer initiateTransfer(@NonNull UUID sourceAccountId, @NonNull UUID destinationAccountId, @NonNull BigDecimal amount) {
        Transfer transferOp = null;
        
        try {
            // Charger les comptes
            Account source = accountService.getAccount(sourceAccountId);
            Account destination = accountService.getAccount(destinationAccountId);

            // CA-04.2 : source == destination
            if (source.getId().equals(destination.getId())) {
                throw new BusinessException(
                        BusinessError.TRANSFER_SAME_ACCOUNT,
                        source.getId().toString()
                );
            }

            // CA-04.5 : compte destination fermé
            if (destination.isClosed()) {
                throw new BusinessException(
                        BusinessError.ACCOUNT_CLOSED,
                        destination.getId().toString()
                );
            }

            // Invariants métier (dans Account)
            source.debit(amount);
            destination.credit(amount);
            // Persistance des comptes
            accountService.updateAccount(source);
            accountService.updateAccount(destination);
            
            transferOp = new Transfer(source.getId(), destination.getId(), amount);

            // Détermination du statut (règle de cas d’usage)
            if (amount.compareTo(seuilSensibilite) <= 0) {
                transferOp.setStatus(OperationStatus.COMPLETED);
            } else {
                transferOp.setStatus(OperationStatus.PENDING);
            }
            
            persistSafely(transferOp);
            
        } catch (BusinessException be) {
            // Erreur métier → FAILED
            Transfer failed = new Transfer(sourceAccountId, destinationAccountId, amount);
            failed.setStatus(OperationStatus.FAILED);
            persistSafely(failed);
            throw be; // on remonte l'erreur métier
        } catch (Exception e) {
            e.printStackTrace();
            // Erreur technique → FAILED
            Transfer failed = new Transfer(sourceAccountId, destinationAccountId, amount);
            failed.setStatus(OperationStatus.FAILED);
            persistSafely(failed);
            throw new TechnicalException(TechnicalError.TRANSFER_FAILED, e);
        }

        return transferOp;
    }

    @Override
    @Transactional
    public TransferValidationResult validateTransfer(TransferValidationRequest transferValidationRequest ) {

        // Charger l'opération persistée
        OperationEntity entity = operationRepository.findById(transferValidationRequest.transferId())
                .orElseThrow(() -> new BusinessException(
                        BusinessError.TRANSFER_NOT_FOUND,
                        transferValidationRequest.transferId().toString()
                ));

        // Mapper vers le domaine
        Transfer transfer = (Transfer) OperationMapper.toDomain(entity);

        // Vérifier état PENDING
        if (transfer.getStatus() != OperationStatus.PENDING) {
            throw new BusinessException(
                    BusinessError.INVALID_TRANSFER_STATUS,
                    transfer.getStatus().name()
            );
        }

        ValidationDecision decision = TransferApiMapper.toValidationDecision(transferValidationRequest.decision());

        // Appliquer la décision
        switch (decision) {
            case APPROVE -> transfer.setStatus(OperationStatus.COMPLETED);
            case REJECT -> transfer.setStatus(OperationStatus.FAILED);
            default -> throw new TechnicalException(
                    TechnicalError.UNSUPPORTED_DECISION,
                    decision.name()
            );
        }

        // Persister la mise à jour
        OperationEntity updated = OperationMapper.toEntity(transfer);
        operationRepository.save(updated);

        // Historisation (pas encore implémentée)
        auditService.recordTransferValidation(updated.getId(), decision);

        return TransferApiMapper.toTransferValidationResult(transfer);
        
    }

}