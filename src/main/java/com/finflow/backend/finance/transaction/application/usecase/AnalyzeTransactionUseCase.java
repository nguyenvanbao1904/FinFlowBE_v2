package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.finance.transaction.domain.entity.Category;
import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import com.finflow.backend.finance.transaction.domain.enums.CategoryType;
import com.finflow.backend.finance.transaction.domain.repository.CategoryRepository;
import com.finflow.backend.finance.transaction.domain.repository.TransactionRepository;
import com.finflow.backend.finance.wealth.domain.entity.WealthAccount;
import com.finflow.backend.finance.wealth.domain.repository.WealthAccountRepository;
import com.finflow.backend.finance.transaction.application.command.AnalyzeTransactionCommand;
import com.finflow.backend.finance.transaction.application.port.in.AnalyzeTransactionPort;
import com.finflow.backend.finance.transaction.presentation.response.AnalyzeTransactionResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyzeTransactionUseCase implements AnalyzeTransactionPort {

    private final CategoryRepository categoryRepository;
    private final WealthAccountRepository wealthAccountRepository;
    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;

    @Value("${data.ai.base-url:http://localhost:8001}")
    private String dataAiBaseUrl;

    @Value("${data.ai.internal-api-key:}")
    private String dataAiInternalApiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Entry point: loads data inside a short read-only transaction,
     * then releases the DB connection before calling the external AI service.
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Override
    public AnalyzeTransactionResponse execute(String userId, AnalyzeTransactionCommand command) {
        log.info("Analyzing transaction text for userId: '{}' text: '{}'", userId, command.text());

        DbSnapshot snapshot = loadDbData(userId);

        try {
            Map<String, Object> aiResponse = callDataAi(command.text(), snapshot);
            return mapAiResponseToAnalyzeResponse(aiResponse, snapshot.categories, snapshot.accounts);
        } catch (Exception e) {
            log.warn("AI prefill failed, fallback to local heuristic. reason={}", e.getMessage());
            return fallbackResponse(snapshot.categories);
        }
    }

    /**
     * Short read-only transaction — loads categories, accounts, and recent transactions
     * then returns immediately so the DB connection is released.
     */
    @Transactional(readOnly = true)
    public DbSnapshot loadDbData(String userId) {
        List<Category> categories = categoryRepository.findByUserIdOrSystem(userId);
        List<WealthAccount> accounts = wealthAccountRepository.findAllByUserIdWithType(userId);
        List<Transaction> recentTransactions = transactionRepository
                .findByUserIdOrderByTransactionDateDescCreatedAtDesc(userId, PageRequest.of(0, 20))
                .getContent();
        return new DbSnapshot(categories, accounts, recentTransactions);
    }

    record DbSnapshot(
            List<Category> categories,
            List<WealthAccount> accounts,
            List<Transaction> recentTransactions
    ) {}

    private Map<String, Object> callDataAi(String rawText, DbSnapshot snapshot) throws Exception {
        List<Map<String, ?>> categoryPayload = snapshot.categories.stream()
                .map(c -> Map.of(
                        "id", c.getId().toString(),
                        "name", c.getName(),
                        "type", c.getType().name()
                ))
                .collect(Collectors.toList());

        List<Map<String, ?>> accountPayload = snapshot.accounts.stream()
                .map(a -> Map.of(
                        "id", a.getId().toString(),
                        "name", a.getName(),
                        "transactionEligible", Boolean.TRUE.equals(a.getWealthAccountType().getIsTransactionEligible())
                ))
                .collect(Collectors.toList());

        List<Map<String, ?>> historyPayload = snapshot.recentTransactions.stream()
                .map(t -> Map.of(
                        "amount", t.getAmount(),
                        "type", t.getType().name(),
                        "categoryId", t.getCategory().getId().toString(),
                        "accountId", t.getWealthAccount().getId().toString(),
                        "note", t.getNote() == null ? "" : t.getNote(),
                        "transactionDate", t.getTransactionDate().toString()
                ))
                .collect(Collectors.toList());

        Map<String, Object> body = Map.of(
                "rawText", rawText,
                "categories", categoryPayload,
                "accounts", accountPayload,
                "recentHistory", historyPayload,
                "locale", "vi-VN",
                "timezone", "Asia/Ho_Chi_Minh",
                "source", "text"
        );

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(dataAiBaseUrl + "/api/v1/ai/transaction-prefill"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));

        if (dataAiInternalApiKey != null && !dataAiInternalApiKey.isBlank()) {
            requestBuilder.header("X-Internal-Api-Key", dataAiInternalApiKey);
        }

        HttpResponse<String> response = httpClient
                .send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("data_ai_service returned " + response.statusCode());
        }

        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }

    private AnalyzeTransactionResponse mapAiResponseToAnalyzeResponse(
            Map<String, Object> aiResponse,
            List<Category> categories,
            List<WealthAccount> accounts
    ) {
        BigDecimal amount = parseBigDecimal(aiResponse.get("amount"));
        CategoryType type = parseCategoryType(aiResponse.get("type"));
        String suggestedCategoryId = parseString(aiResponse.get("categoryId"));
        String suggestedAccountId = parseString(aiResponse.get("accountId"));
        String note = parseString(aiResponse.get("note"));
        LocalDateTime transactionDate = parseDateTime(aiResponse.get("transactionDate"));

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

        return AnalyzeTransactionResponse.builder()
                .amount(amount)
                .type(type)
                .suggestedCategoryId(suggestedCategoryId)
                .suggestedAccountId(suggestedAccountId)
                .note(note)
                .transactionDate(transactionDate)
                .build();
    }

    private AnalyzeTransactionResponse fallbackResponse(List<Category> categories) {
        Category firstExpense = categories.stream()
                .filter(c -> c.getType() == CategoryType.EXPENSE)
                .findFirst()
                .orElse(null);
        String suggestedCategoryId = firstExpense != null ? firstExpense.getId().toString() : null;

        return AnalyzeTransactionResponse.builder()
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

    private boolean containsEligibleAccount(List<WealthAccount> accounts, String accountId) {
        try {
            UUID id = UUID.fromString(accountId);
            return accounts.stream()
                    .anyMatch(a ->
                            a.getId().equals(id)
                                    && a.getWealthAccountType() != null
                                    && Boolean.TRUE.equals(a.getWealthAccountType().getIsTransactionEligible()));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) return null;
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private CategoryType parseCategoryType(Object value) {
        if (value == null) return null;
        try {
            return CategoryType.valueOf(String.valueOf(value).trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String parseString(Object value) {
        if (value == null) return null;
        String s = String.valueOf(value).trim();
        return s.isEmpty() ? null : s;
    }

    private LocalDateTime parseDateTime(Object value) {
        if (value == null) return null;
        String raw = String.valueOf(value).trim();
        if (raw.isEmpty()) return null;
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
