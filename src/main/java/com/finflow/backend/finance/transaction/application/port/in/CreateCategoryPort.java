package com.finflow.backend.finance.transaction.application.port.in;

import com.finflow.backend.finance.transaction.application.command.CreateCategoryCommand;
import com.finflow.backend.finance.transaction.application.dto.CategoryOutput;

public interface CreateCategoryPort {
    CategoryOutput execute(CreateCategoryCommand command);
}
