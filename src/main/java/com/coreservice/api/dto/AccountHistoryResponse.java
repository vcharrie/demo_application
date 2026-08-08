package com.coreservice.api.dto;

import java.util.List;
import java.util.UUID;

public record AccountHistoryResponse(
        UUID accountId,
        List<Operation> operations
) { }
