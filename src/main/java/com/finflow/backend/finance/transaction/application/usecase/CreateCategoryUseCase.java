package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.finance.transaction.application.port.in.CreateCategoryPort;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.common.enums.CategoryType;
import com.finflow.backend.finance.transaction.domain.entity.Category;
import com.finflow.backend.finance.transaction.domain.repository.CategoryRepository;
import com.finflow.backend.finance.transaction.application.command.CreateCategoryCommand;
import com.finflow.backend.finance.transaction.exception.TransactionErrorCode;
import com.finflow.backend.finance.transaction.application.dto.CategoryOutput;
import com.finflow.backend.finance.transaction.application.mapper.CategoryMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreateCategoryUseCase implements CreateCategoryPort {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    @Override
    public CategoryOutput execute(CreateCategoryCommand command) {
        String userId = command.userId();
        log.info("Creating category for userId: {}", userId);

        CategoryType categoryType;
        try {
            categoryType = CategoryType.valueOf(command.type());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid category type value: {}", command.type());
            throw new AppException(TransactionErrorCode.INVALID_CATEGORY_TYPE);
        }

        String icon = command.icon() != null ? command.icon().trim() : null;
        if (icon == null || icon.isBlank()) {
            icon = Category.DEFAULT_ICON;
        }

        String color = command.color() != null ? command.color().trim() : null;
        if (color == null || color.isBlank()) {
            color = Category.DEFAULT_COLOR;
        }

        Category category = Category.builder()
                .userId(userId)
                .name(command.name())
                .type(categoryType)
                .icon(icon)
                .color(color)
                .isSystem(false)
                .build();

        Category saved = categoryRepository.save(category);
        return categoryMapper.toCategoryOutput(saved);
    }
}
