package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.finance.transaction.domain.entity.Category;
import com.finflow.backend.finance.transaction.domain.enums.CategoryType;
import com.finflow.backend.finance.transaction.domain.repository.CategoryRepository;
import com.finflow.backend.finance.transaction.presentation.request.AnalyzeTransactionRequest;
import com.finflow.backend.finance.transaction.presentation.response.AnalyzeTransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyzeTransactionUseCase {

    private final CategoryRepository categoryRepository;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public AnalyzeTransactionResponse execute(String userId, AnalyzeTransactionRequest request) {
        log.info("Analyzing transaction text for userId: '{}' text: '{}'", userId, request.getText());

        List<Category> categories = categoryRepository.findByUserIdOrSystem(userId);
        Category firstExpense = categories.stream()
                .filter(c -> c.getType() == CategoryType.EXPENSE)
                .findFirst()
                .orElse(null);
        String suggestedCategoryId = firstExpense != null ? firstExpense.getId().toString() : null;

        return AnalyzeTransactionResponse.builder()
                .amount(null)
                .type(CategoryType.EXPENSE)
                .suggestedCategoryId(suggestedCategoryId)
                .note(null)
                .transactionDate(LocalDateTime.now())
                .build();
    }
}
