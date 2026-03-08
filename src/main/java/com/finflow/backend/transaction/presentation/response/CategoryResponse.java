package com.finflow.backend.transaction.presentation.response;

import com.finflow.backend.transaction.domain.entity.CategoryType;
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
}
