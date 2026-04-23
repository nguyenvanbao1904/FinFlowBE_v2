package com.finflow.backend.finance.transaction.presentation.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCategoryRequest {

    @NotBlank(message = "CATEGORY_NAME_REQUIRED")
    String name;

    @NotBlank(message = "CATEGORY_TYPE_REQUIRED")
    String type;

    String icon;

    String color;
}
