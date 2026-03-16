package com.finflow.backend.finance.transaction.presentation.response;

import com.finflow.backend.finance.transaction.domain.enums.CategoryType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryResponse {
    UUID id;
    String name;
    CategoryType type;
    String icon;
    String color;
    /**
     * True if this category is defined by the system and cannot be edited or deleted by the user.
     */
    boolean systemCategory;
}
