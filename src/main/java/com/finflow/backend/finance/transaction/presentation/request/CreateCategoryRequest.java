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

    @NotBlank(message = "CATEGORY_NAME_REQUIRED")
    String name;

    @NotNull(message = "CATEGORY_TYPE_REQUIRED")
    CategoryType type;

    String icon;

    String color;
}
