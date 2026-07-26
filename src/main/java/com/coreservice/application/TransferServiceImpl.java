package com.coreservice.application;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.coreservice.application.exception.TechnicalError;
import com.coreservice.application.exception.TechnicalException;
import com.coreservice.config.TransferProperties;
import com.coreservice.domain.Account;
import com.coreservice.domain.OperationStatus;
import com.coreservice.domain.Transfer;
import com.coreservice.domain.exception.BusinessError;
import com.coreservice.domain.exception.BusinessException;
import com.coreservice.infrastructure.entity.OperationEntity;
import com.coreservice.infrastructure.mapper.OperationMapper;
import com.coreservice.infrastructure.repository.OperationRepository;

import jakarta.transaction.Transactional;

@Service
public class TransferServiceImpl implements TransferService {

    private final AccountService accountService;
    //private final AuditService auditEventService;
    private final BigDecimal seuilSensibilite;
    private final OperationRepository operationRepository;
    

    public TransferServiceImpl(AccountService accountService,
                               AuditService auditEventService,
                               TransferProperties transferProperties,
                               OperationRepository operationRepository) {
        this.accountService = accountService;
        //this.auditEventService = auditEventService;
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
    public Transfer initiateTransfer(UUID sourceAccountId, UUID destinationAccountId, BigDecimal amount) {
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
            // Erreur technique → FAILED
            Transfer failed = new Transfer(sourceAccountId, destinationAccountId, amount);
            failed.setStatus(OperationStatus.FAILED);
            persistSafely(failed);
            throw new TechnicalException(TechnicalError.TRANSFER_FAILED, e);
        }

        return transferOp;
    }

}