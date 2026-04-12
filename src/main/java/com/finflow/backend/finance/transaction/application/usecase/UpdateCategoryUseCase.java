package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.finance.transaction.application.port.in.UpdateCategoryPort;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.transaction.application.mapper.CategoryMapper;
import com.finflow.backend.finance.transaction.domain.entity.Category;
import com.finflow.backend.finance.transaction.domain.repository.CategoryRepository;
import com.finflow.backend.finance.transaction.exception.TransactionErrorCode;
import com.finflow.backend.finance.transaction.application.command.UpdateCategoryCommand;
import com.finflow.backend.finance.transaction.presentation.response.CategoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateCategoryUseCase implements UpdateCategoryPort {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Override
    public CategoryResponse execute(UpdateCategoryCommand command) {
        String userId = command.userId();
        UUID categoryId = command.categoryId();
        log.info("Updating category {} for userId: {}", categoryId, userId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(TransactionErrorCode.CATEGORY_NOT_FOUND));

        if (!category.getUserId().equals(userId) || Boolean.TRUE.equals(category.getIsSystem())) {
            throw new AppException(TransactionErrorCode.CATEGORY_NOT_OWNED);
        }

        category.setName(command.name().trim());
        if (command.icon() != null) {
            String icon = command.icon().trim();
            if (icon.isBlank()) {
                icon = Category.DEFAULT_ICON;
            }
            category.setIcon(icon);
        }
        category.setColor(command.color() != null ? command.color().trim() : category.getColor());

        Category saved = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(saved);
    }
}
