package com.finflow.backend.finance.wealth.application.port.in;

import com.finflow.backend.finance.wealth.application.command.DeleteWealthAccountCommand;

public interface DeleteWealthAccountPort {
    void execute(DeleteWealthAccountCommand command);
}
