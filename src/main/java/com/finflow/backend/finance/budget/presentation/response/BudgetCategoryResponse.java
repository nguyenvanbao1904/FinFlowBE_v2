package com.finflow.backend.finance.budget.presentation.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BudgetCategoryResponse {
    UUID id;
    String name;
    String type;
    String icon;
    String color;
    boolean systemCategory;
}
