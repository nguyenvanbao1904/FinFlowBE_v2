package com.finflow.backend.finance.budget.application.mapper;

import com.finflow.backend.finance.budget.domain.entity.Budget;
import com.finflow.backend.finance.budget.presentation.response.BudgetResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {com.finflow.backend.finance.transaction.application.mapper.CategoryMapper.class})
public interface BudgetMapper {

    @Mapping(target = "spentAmount", ignore = true)
    BudgetResponse toBudgetResponse(Budget budget);
}

