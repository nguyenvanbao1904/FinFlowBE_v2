package com.finflow.backend.finance.budget.application.mapper;

import com.finflow.backend.finance.budget.domain.entity.Budget;
import com.finflow.backend.finance.budget.presentation.response.BudgetResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {com.finflow.backend.finance.transaction.application.mapper.CategoryMapper.class})
public interface BudgetMapper {

    BudgetResponse toBudgetResponse(Budget budget);
}

