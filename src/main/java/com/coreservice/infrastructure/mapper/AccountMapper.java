package com.coreservice.infrastructure.mapper;

import org.springframework.lang.NonNull;

import com.coreservice.domain.Account;
import com.coreservice.infrastructure.entity.AccountEntity;


public class AccountMapper {

    @NonNull
    public static AccountEntity toEntity(Account account) {
        return new AccountEntity(
                account.getOwnerId(),
                account.getBalance(),
                account.getStatus());
    }

    @NonNull
    public static Account toDomain(AccountEntity entity) {
        return new Account(
                entity.getId(),
                entity.getOwnerId(),
                entity.getBalance(),
                entity.getStatus());
    }

}
