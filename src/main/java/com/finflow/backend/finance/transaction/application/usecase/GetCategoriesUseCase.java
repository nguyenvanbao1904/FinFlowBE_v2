package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.finance.transaction.application.port.in.GetCategoriesPort;
import com.finflow.backend.finance.transaction.application.query.GetCategoriesQuery;

import com.finflow.backend.finance.transaction.application.dto.CategoryOutput;
import com.finflow.backend.finance.transaction.application.mapper.CategoryMapper;
import com.finflow.backend.finance.transaction.domain.entity.Category;
import com.finflow.backend.finance.transaction.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetCategoriesUseCase implements GetCategoriesPort {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    @Override
    public List<CategoryOutput> execute(GetCategoriesQuery request) {
        String userId = request.userId();
        log.info("Fetching categories for userId: {}", userId);
        List<Category> categories = categoryRepository.findByUserIdOrSystem(userId);
        return categories.stream()
                .map(categoryMapper::toCategoryOutput)
                .collect(Collectors.toList());
    }
}
