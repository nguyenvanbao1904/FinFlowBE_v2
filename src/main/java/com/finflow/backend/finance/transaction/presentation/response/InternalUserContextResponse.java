package com.finflow.backend.finance.transaction.presentation.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Presentation DTO for the internal AI-agent user-context endpoint.
 * Mirrors {@code InternalTransactionUserContextOutput} structure.
 */
public record InternalUserContextResponse(
        List<ContextCategory> categories,
        List<ContextAccount> accounts
) {
    public record ContextCategory(String id, String name, String type, String icon) {}

    public record ContextAccount(String id, String name, String type, BigDecimal balance) {}
}
