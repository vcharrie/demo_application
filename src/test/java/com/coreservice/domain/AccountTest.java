package com.coreservice.domain;

import org.junit.jupiter.api.Test;

import com.coreservice.domain.exception.BusinessError;
import com.coreservice.domain.exception.BusinessException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void debitShouldThrowWhenAmountIsNullOrZeroOrNegative() {
        Account account = new Account(
                UUID.randomUUID(),
                new BigDecimal("100.00")
        );

        assertThrows(BusinessException.class,
                () -> account.debit(null));

        assertThrows(BusinessException.class,
                () -> account.debit(BigDecimal.ZERO));

        assertThrows(BusinessException.class,
                () -> account.debit(new BigDecimal("-10")));
    }
 
    @Test
    void debitShouldThrowWhenAccountIsSuspended() {
        Account account = new Account(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                AccountStatus.SUSPENDED
        );

        BusinessException ex = assertThrows(BusinessException.class,
                () -> account.debit(new BigDecimal("10")));

        assertEquals(BusinessError.ACCOUNT_SUSPENDED, ex.getError());
    }

    @Test
    void debitShouldThrowWhenBalanceIsInsufficient() {
         Account account = new Account(
                UUID.randomUUID(),
                new BigDecimal("100.00")
        );

        BusinessException ex = assertThrows(BusinessException.class,
                () -> account.debit(new BigDecimal("100.01")));

        assertEquals(BusinessError.ACCOUNT_INSUFFICIENT_FUNDS, ex.getError());
    }


    @Test
    void debitShouldDecreaseBalanceWhenValid() {
          Account account = new Account(
                UUID.randomUUID(),
                new BigDecimal("100.00")
        );

        account.debit(new BigDecimal("30"));

        assertEquals(new BigDecimal("70.00"), account.getBalance());
    }
}
