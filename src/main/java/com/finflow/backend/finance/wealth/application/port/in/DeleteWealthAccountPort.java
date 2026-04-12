package com.finflow.backend.finance.wealth.application.port.in;

import com.finflow.backend.finance.wealth.domain.entity.WealthAccount;
import com.finflow.backend.finance.wealth.application.command.DeleteWealthAccountCommand;
import java.util.UUID;

public interface DeleteWealthAccountPort {
    void execute(DeleteWealthAccountCommand command);
}
