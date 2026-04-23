package com.finflow.backend.finance.transaction.application.mapper;

import com.finflow.backend.finance.transaction.application.dto.CategoryOutput;
import com.finflow.backend.finance.transaction.domain.constant.TransactionConstants;
import com.finflow.backend.finance.transaction.domain.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "systemCategory", expression = "java(isSystemCategory(category))")
    CategoryOutput toCategoryOutput(Category category);

    /**
     * System categories are owned by the special SYSTEM user.
     */
    default boolean isSystemCategory(Category category) {
        return TransactionConstants.SYSTEM_USER_ID.equalsIgnoreCase(category.getUserId());
    }
}
