package com.coreservice.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.coreservice.domain.Account;

import lombok.NonNull;

public interface AccountService {

    public Account createAccount(UUID ownerId, BigDecimal initialBalance);

    public List<Account> findAll();

    public Account findById(@NonNull UUID id);

    public void delete(@NonNull UUID id);

    public Account deposit(@NonNull UUID accountId, @NonNull BigDecimal amount);
}
