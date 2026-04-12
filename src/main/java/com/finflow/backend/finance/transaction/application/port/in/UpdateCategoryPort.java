package com.finflow.backend.finance.transaction.application.port.in;

import com.finflow.backend.finance.transaction.application.command.UpdateCategoryCommand;
import com.finflow.backend.finance.transaction.presentation.response.CategoryResponse;

public interface UpdateCategoryPort {
    CategoryResponse execute(UpdateCategoryCommand command);
}
