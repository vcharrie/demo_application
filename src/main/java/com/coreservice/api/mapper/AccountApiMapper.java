package com.coreservice.api.mapper;

import com.coreservice.api.dto.AccountRequest;
import com.coreservice.api.dto.AccountResponse;
import com.coreservice.domain.Account;

public final class AccountApiMapper {

    private AccountApiMapper() { }

    public static Account toDomain(AccountRequest request) {
        return new Account(
                request.ownerId(),
                request.initialBalance()
        );
    }

    public static AccountResponse toResponse(Account domain) {
        return new AccountResponse(
                domain.getId(),
                domain.getOwnerId(),
                domain.getBalance()
        );
    }
}
