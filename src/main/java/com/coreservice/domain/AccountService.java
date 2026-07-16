package com.coreservice.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountService {

    public Account createAccount(UUID ownerId, BigDecimal initialBalance);

    public List<Account> findAll();

    public Account findById(UUID id);

    public void delete(UUID id);

}
