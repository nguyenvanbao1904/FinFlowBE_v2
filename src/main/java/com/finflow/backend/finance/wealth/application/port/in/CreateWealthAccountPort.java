package com.finflow.backend.finance.wealth.application.port.in;

import com.finflow.backend.finance.wealth.application.command.CreateWealthAccountCommand;
import com.finflow.backend.finance.wealth.application.dto.WealthAccountOutput;

public interface CreateWealthAccountPort {
    WealthAccountOutput execute(CreateWealthAccountCommand command);
}
