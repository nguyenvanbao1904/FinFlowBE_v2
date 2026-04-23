package com.finflow.backend.finance.transaction.application.query;

public record AnalyzeTransactionQuery(
        String userId,
        String text
) {}
