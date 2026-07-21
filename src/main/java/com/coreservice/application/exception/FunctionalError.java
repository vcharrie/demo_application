package com.coreservice.application.exception;

public enum FunctionalError {

    ACCOUNT_NOT_FOUND("FUNC_ACCOUNT_NOT_FOUND", "Account %s not found"),
    ACCOUNT_ALREADY_EXISTS("FUNC_ACCOUNT_ALREADY_EXISTS", "Account already exists for owner %s"),
    INVALID_REQUEST("FUNC_INVALID_REQUEST", "Invalid request: %s"),
    MISSING_PARAMETER("FUNC_MISSING_PARAMETER", "Missing parameter: %s"),
    UNAUTHORIZED_OPERATION("FUNC_UNAUTHORIZED_OPERATION", "Operation not allowed: %s");

    private final String code;
    private final String template;

    FunctionalError(String code, String template) {
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
