package com.finflow.backend.finance.budget.presentation.mapper;

import com.finflow.backend.finance.budget.application.dto.BudgetCategoryOutput;
import com.finflow.backend.finance.budget.application.dto.BudgetOutput;
import com.finflow.backend.finance.budget.presentation.response.BudgetCategoryResponse;
import com.finflow.backend.finance.budget.presentation.response.BudgetResponse;
import com.finflow.backend.finance.common.enums.CategoryType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE,
        unmappedSourcePolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface BudgetPresentationMapper {

    BudgetResponse toResponse(BudgetOutput output);

    @Mapping(target = "type", source = "type", qualifiedByName = "categoryTypeToString")
    BudgetCategoryResponse toResponse(BudgetCategoryOutput output);

    List<BudgetResponse> toResponses(List<BudgetOutput> outputs);

    @Named("categoryTypeToString")
    default String categoryTypeToString(CategoryType type) {
        return type != null ? type.name() : null;
    }
}
