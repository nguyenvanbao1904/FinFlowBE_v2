package com.finflow.backend.transaction.application.usecase;

import com.finflow.backend.transaction.application.mapper.CategoryMapper;
import com.finflow.backend.transaction.domain.entity.Category;
import com.finflow.backend.transaction.domain.repository.CategoryRepository;
import com.finflow.backend.transaction.presentation.response.CategoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetCategoriesUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<CategoryResponse> execute(String userId) {
        log.info("Fetching categories for userId: {}", userId);
        List<Category> categories = categoryRepository.findByUserIdOrSystem(userId);
        return categories.stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }
}
