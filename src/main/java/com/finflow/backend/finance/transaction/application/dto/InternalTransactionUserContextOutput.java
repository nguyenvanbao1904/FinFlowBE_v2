package com.finflow.backend.finance.transaction.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record InternalTransactionUserContextOutput(
        List<ContextCategory> categories,
        List<ContextAccount> accounts
) {
    public record ContextCategory(String id, String name, String type, String icon) {}

    public record ContextAccount(String id, String name, String type, BigDecimal balance) {}
}
