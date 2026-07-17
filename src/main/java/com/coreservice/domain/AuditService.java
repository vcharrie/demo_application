package com.coreservice.domain;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public interface AuditService {

    @Transactional
    public void recordAccountCreation(Account account);
}