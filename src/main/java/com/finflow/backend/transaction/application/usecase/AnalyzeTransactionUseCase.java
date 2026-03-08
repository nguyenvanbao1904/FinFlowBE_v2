package com.finflow.backend.transaction.application.usecase;

import com.finflow.backend.transaction.domain.entity.Category;
import com.finflow.backend.transaction.domain.entity.CategoryType;
import com.finflow.backend.transaction.domain.repository.CategoryRepository;
import com.finflow.backend.transaction.presentation.request.AnalyzeTransactionRequest;
import com.finflow.backend.transaction.presentation.response.AnalyzeTransactionResponse;
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
        
        // Mocking AI analysis for now. Returns a fixed response as requested by the user.
        List<Category> categories = categoryRepository.findByUserIdOrSystem(userId);
        
        // Try to find a Di chuyển or Ăn uống category for a more realistic mock
        Category defaultCategory = categories.stream()
                .filter(c -> c.getName().toLowerCase().contains("di chuyển") || c.getName().toLowerCase().contains("xăng"))
                .findFirst()
                .orElse(categories.isEmpty() ? null : categories.get(0));

        String categoryId = defaultCategory != null ? defaultCategory.getId().toString() : null;

        return AnalyzeTransactionResponse.builder()
                .amount(new BigDecimal("50000"))
                .type(CategoryType.EXPENSE)
                .suggestedCategoryId(categoryId)
                .note("Đổ xăng")
                .transactionDate(LocalDateTime.now())
                .build();
    }
}
