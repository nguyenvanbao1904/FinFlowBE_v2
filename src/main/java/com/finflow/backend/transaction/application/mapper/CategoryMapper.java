package com.finflow.backend.transaction.application.mapper;

import com.finflow.backend.transaction.domain.entity.Category;
import com.finflow.backend.transaction.presentation.response.CategoryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toCategoryResponse(Category category);
}
