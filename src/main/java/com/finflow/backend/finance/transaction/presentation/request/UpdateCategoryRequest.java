package com.finflow.backend.finance.transaction.presentation.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCategoryRequest {

    @NotBlank(message = "Category name is required")
    String name;

    String icon;

    String color;
}
