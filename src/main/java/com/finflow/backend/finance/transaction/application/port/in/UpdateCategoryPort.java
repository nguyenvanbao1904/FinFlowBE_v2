package com.finflow.backend.finance.transaction.application.port.in;

import com.finflow.backend.finance.transaction.application.command.UpdateCategoryCommand;
import com.finflow.backend.finance.transaction.application.dto.CategoryOutput;

public interface UpdateCategoryPort {
    CategoryOutput execute(UpdateCategoryCommand command);
}
