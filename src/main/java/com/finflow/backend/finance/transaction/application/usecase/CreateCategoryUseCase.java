package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.finance.transaction.application.mapper.CategoryMapper;
import com.finflow.backend.finance.transaction.domain.entity.Category;
import com.finflow.backend.finance.transaction.domain.repository.CategoryRepository;
import com.finflow.backend.finance.transaction.presentation.request.CreateCategoryRequest;
import com.finflow.backend.finance.transaction.presentation.response.CategoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public CategoryResponse execute(String userId, CreateCategoryRequest request) {
        log.info("Creating category for userId: {}", userId);

        String icon = request.getIcon() != null ? request.getIcon().trim() : null;
        if (icon == null || icon.isBlank()) {
            icon = Category.DEFAULT_ICON;
        }

        Category category = Category.builder()
                .userId(userId)
                .name(request.getName().trim())
                .type(request.getType())
                .icon(icon)
                .color(request.getColor() != null ? request.getColor().trim() : null)
                .build();

        Category saved = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(saved);
    }
}
