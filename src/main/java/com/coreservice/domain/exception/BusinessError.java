package com.coreservice.domain.exception;

public enum BusinessError {

    ACCOUNT_OWNER_ID_NULL("BUS_ACCOUNT_OWNER_ID_NULL", "Account owner ID cannot be null"),
    AMOUNT_INVALID("BUS_AMOUNT_INVALID", "Amount %s is invalid"),
    ACCOUNT_SUSPENDED("BUS_ACCOUNT_SUSPENDED", "Account %s is suspended"),
    ACCOUNT_CLOSED("BUS_ACCOUNT_CLOSED", "Account %s is closed"),
    ACCOUNT_INSUFFICIENT_FUNDS("BUS_INSUFFICIENT_FUNDS", "Insufficient funds: balance=%s, attempted debit=%s"),
    TRANSFER_SAME_ACCOUNT("BUS_TRANSFER_SAME_ACCOUNT", "Transfer failed: source and destination accounts are the same (%s)"),
    TRANSFER_NOT_FOUND("BUS_TRANSFER_NOT_FOUND", "Transfer not found: %s"),
    INVALID_TRANSFER_STATUS("BUS_INVALID_TRANSFER_STATUS", "Invalid transfer status: %s");

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
