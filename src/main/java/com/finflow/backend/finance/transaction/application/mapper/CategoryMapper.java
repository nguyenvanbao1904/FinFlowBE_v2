package com.finflow.backend.finance.transaction.application.mapper;

import com.finflow.backend.finance.transaction.domain.entity.Category;
import com.finflow.backend.finance.transaction.presentation.response.CategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "systemCategory", expression = "java(isSystemCategory(category))")
    CategoryResponse toCategoryResponse(Category category);

    /**
     * System categories are owned by the special SYSTEM user.
     */
    default boolean isSystemCategory(Category category) {
        return "SYSTEM".equalsIgnoreCase(category.getUserId());
    }
}
