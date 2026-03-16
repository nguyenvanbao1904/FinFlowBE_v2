package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.transaction.application.mapper.CategoryMapper;
import com.finflow.backend.finance.transaction.domain.entity.Category;
import com.finflow.backend.finance.transaction.domain.repository.CategoryRepository;
import com.finflow.backend.finance.transaction.exception.TransactionErrorCode;
import com.finflow.backend.finance.transaction.presentation.request.UpdateCategoryRequest;
import com.finflow.backend.finance.transaction.presentation.response.CategoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public CategoryResponse execute(String userId, UUID categoryId, UpdateCategoryRequest request) {
        log.info("Updating category {} for userId: {}", categoryId, userId);

        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new AppException(TransactionErrorCode.CATEGORY_NOT_FOUND));

        if ("SYSTEM".equals(category.getUserId())) {
            throw new AppException(TransactionErrorCode.CATEGORY_NOT_OWNED);
        }

        category.setName(request.getName().trim());
        if (request.getIcon() != null) {
            String icon = request.getIcon().trim();
            if (icon.isBlank()) {
                icon = Category.DEFAULT_ICON;
            }
            category.setIcon(icon);
        }
        category.setColor(request.getColor() != null ? request.getColor().trim() : category.getColor());

        Category saved = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(saved);
    }
}
