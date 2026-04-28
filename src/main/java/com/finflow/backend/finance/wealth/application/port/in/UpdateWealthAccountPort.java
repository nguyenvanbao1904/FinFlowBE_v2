package com.finflow.backend.finance.wealth.application.port.in;

import com.finflow.backend.finance.wealth.application.command.UpdateWealthAccountCommand;
import com.finflow.backend.finance.wealth.application.dto.WealthAccountOutput;

public interface UpdateWealthAccountPort {
    WealthAccountOutput execute(UpdateWealthAccountCommand command);
}
