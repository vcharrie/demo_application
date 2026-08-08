package com.coreservice.application.exception;

public enum TechnicalError {

    DATABASE_ERROR("TECH_DB_001", "A database error occurred"),
    NETWORK_ERROR("TECH_NET_001", "A network error occurred"),
    TIMEOUT_ERROR("TECH_TIMEOUT_001", "A timeout occurred"),
    IO_ERROR("TECH_IO_001", "An I/O error occurred"),
    UNKNOWN_ERROR("TECH_UNKNOWN_001", "An unknown technical error occurred"),
    TRANSFER_FAILED("TECH_TRANSFER_001", "Transfer operation failed"),
    UNSUPPORTED_DECISION("TECH_UNSUPPORTED_DECISION", "Unsupported validation decision: %s");

    private final String code;
    private final String template;

    TechnicalError(String code, String template) {
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
