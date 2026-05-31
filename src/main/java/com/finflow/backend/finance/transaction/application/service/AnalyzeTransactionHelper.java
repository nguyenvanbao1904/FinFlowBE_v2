package com.finflow.backend.finance.transaction.application.service;

import com.finflow.backend.finance.transaction.application.dto.AnalyzeTransactionOutput;
import com.finflow.backend.finance.transaction.application.result.TransactionPrefillResult;
import com.finflow.backend.finance.transaction.domain.entity.Category;
import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import com.finflow.backend.finance.common.enums.CategoryType;
import com.finflow.backend.finance.wealth.api.WealthAccountApi;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class AnalyzeTransactionHelper {

    public List<Map<String, ?>> buildCategoryPayload(List<Category> categories) {
        return categories.stream()
                .map(c -> Map.of(
                        "id", c.getId().toString(),
                        "name", c.getName(),
                        "type", c.getType().name()
                ))
                .collect(Collectors.toList());
    }

    public List<Map<String, ?>> buildAccountPayload(List<WealthAccountApi.AccountSnapshot> accounts) {
        return accounts.stream()
                .map(a -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("id", a.id().toString());
                    payload.put("name", a.name());
                    payload.put("typeCode", a.typeCode());
                    payload.put("typeDisplayName", a.typeDisplayName());
                    payload.put("balance", a.balance());
                    payload.put("transactionEligible", a.transactionEligible());
                    payload.put("debt", a.debt());
                    return payload;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, ?>> buildHistoryPayload(List<Transaction> recentTransactions) {
        return recentTransactions.stream()
                .map(t -> Map.of(
                        "amount", t.getAmount(),
                        "type", t.getType().name(),
                        "categoryId", t.getCategory().getId().toString(),
                        "accountId", t.getWealthAccountId().toString(),
                        "note", t.getNote() == null ? "" : t.getNote(),
                        "transactionDate", t.getTransactionDate().toString()
                ))
                .collect(Collectors.toList());
    }

    public AnalyzeTransactionOutput mapPrefillResultToResponse(
            TransactionPrefillResult result,
            List<Category> categories,
            List<WealthAccountApi.AccountSnapshot> accounts
    ) {
        BigDecimal amount = result.amount();
        CategoryType type = parseCategoryType(result.type());
        String suggestedCategoryId = result.categoryId();
        String suggestedAccountId = result.accountId();
        String note = result.note();
        LocalDateTime transactionDate = parseDateTime(result.transactionDate());

        if (suggestedCategoryId != null && !containsCategory(categories, suggestedCategoryId)) {
            suggestedCategoryId = null;
        }
        if (suggestedAccountId != null && !containsEligibleAccount(accounts, suggestedAccountId)) {
            suggestedAccountId = null;
        }

        if (type == null) {
            type = CategoryType.EXPENSE;
        }
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }

        return AnalyzeTransactionOutput.builder()
                .amount(amount)
                .type(type)
                .suggestedCategoryId(suggestedCategoryId)
                .suggestedAccountId(suggestedAccountId)
                .note(note)
                .transactionDate(transactionDate)
                .build();
    }

    public AnalyzeTransactionOutput fallbackResponse(List<Category> categories) {
        Category firstExpense = categories.stream()
                .filter(c -> c.getType() == CategoryType.EXPENSE)
                .findFirst()
                .orElse(null);
        String suggestedCategoryId = firstExpense != null ? firstExpense.getId().toString() : null;

        return AnalyzeTransactionOutput.builder()
                .amount(null)
                .type(CategoryType.EXPENSE)
                .suggestedCategoryId(suggestedCategoryId)
                .suggestedAccountId(null)
                .note(null)
                .transactionDate(LocalDateTime.now())
                .build();
    }

    private boolean containsCategory(List<Category> categories, String categoryId) {
        try {
            UUID id = UUID.fromString(categoryId);
            return categories.stream().anyMatch(c -> c.getId().equals(id));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean containsEligibleAccount(List<WealthAccountApi.AccountSnapshot> accounts, String accountId) {
        try {
            UUID id = UUID.fromString(accountId);
            return accounts.stream()
                    .anyMatch(a -> a.id().equals(id) && a.transactionEligible());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private CategoryType parseCategoryType(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return CategoryType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private LocalDateTime parseDateTime(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return OffsetDateTime.parse(raw).toLocalDateTime();
        } catch (Exception ignored) {
            try {
                return LocalDateTime.parse(raw);
            } catch (Exception ignored2) {
                return null;
            }
        }
    }
}
