package com.finflow.backend.finance.transaction.application.port.in;

import com.finflow.backend.finance.transaction.application.command.DeleteCategoryCommand;

public interface DeleteCategoryPort {
    void execute(DeleteCategoryCommand command);
}
