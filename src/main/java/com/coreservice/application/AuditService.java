package com.coreservice.application;
import java.math.BigDecimal;
import com.coreservice.domain.Account;



public interface AuditService {
    
    public void recordAccountCreation(Account account);
    
    public void recordCredit(Account account, BigDecimal amount);

    public void recordDebit(Account account, BigDecimal amount);

    public void recordTransferInitiation(Account source, Account destination, BigDecimal amount);
    
    public void recordTransferValidation(java.util.UUID transferId, com.coreservice.domain.ValidationDecision decision);
}