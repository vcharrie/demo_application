package com.coreservice.application.exception;

public enum TechnicalError {

    DATABASE_ERROR("TECH_DB_001", "A database error occurred"),
    NETWORK_ERROR("TECH_NET_001", "A network error occurred"),
    TIMEOUT_ERROR("TECH_TIMEOUT_001", "A timeout occurred"),
    IO_ERROR("TECH_IO_001", "An I/O error occurred"),
    UNKNOWN_ERROR("TECH_UNKNOWN_001", "An unknown technical error occurred");

    private final String code;
    private final String message;

    TechnicalError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}
