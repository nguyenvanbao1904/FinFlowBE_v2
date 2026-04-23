package com.finflow.backend.finance.budget.application.mapper;

import com.finflow.backend.finance.budget.application.dto.BudgetOutput;
import com.finflow.backend.finance.budget.domain.entity.Budget;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BudgetMapper {

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "spentAmount", ignore = true)
    BudgetOutput toBudgetOutput(Budget budget);
}

