package com.coreservice.application;


import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coreservice.domain.Account;


@Service
public interface AuditService {

    @Transactional
    public void recordAccountCreation(Account account);

    @Transactional
    public void recordCredit(Account account, BigDecimal amount);
}