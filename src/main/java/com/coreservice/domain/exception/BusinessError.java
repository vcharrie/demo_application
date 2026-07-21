package com.coreservice.domain.exception;

public enum BusinessError {

    ACCOUNT_OWNER_ID_NULL("BUS_ACCOUNT_OWNER_ID_NULL", "Account owner ID cannot be null"),
    AMOUNT_INVALID("BUS_AMOUNT_INVALID", "Amount %s is invalid"),
    ACCOUNT_SUSPENDED("BUS_ACCOUNT_SUSPENDED", "Account %s is suspended"),
    ACCOUNT_INSUFFICIENT_FUNDS("BUS_INSUFFICIENT_FUNDS", "Insufficient funds: balance=%s, attempted debit=%s");

    private final String code;
    private final String template;

    BusinessError(String code, String template) {
        this.code = code;
        this.template = template;
    }

    public String code() {
        return code;
    }

    public String template() {
        return template;
    }
}
