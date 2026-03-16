package com.finflow.backend.finance.transaction.presentation.request;

import com.finflow.backend.finance.transaction.domain.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    String name;

    @NotNull(message = "Category type is required")
    CategoryType type;

    String icon;

    String color;
}
