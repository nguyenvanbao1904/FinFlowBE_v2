package com.finflow.backend.finance.transaction.application.port.in;

import com.finflow.backend.finance.transaction.application.command.CreateCategoryCommand;
import com.finflow.backend.finance.transaction.presentation.response.CategoryResponse;

public interface CreateCategoryPort {
    CategoryResponse execute(CreateCategoryCommand command);
}
