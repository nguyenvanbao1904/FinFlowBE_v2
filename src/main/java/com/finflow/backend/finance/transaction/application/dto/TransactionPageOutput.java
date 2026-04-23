package com.finflow.backend.finance.transaction.application.dto;

import java.util.List;

public record TransactionPageOutput(
        List<TransactionOutput> content,
        long totalElements,
        int totalPages,
        int number,
        int size,
        boolean first,
        boolean last,
        int numberOfElements
) {}

