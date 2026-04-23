package com.finflow.backend.finance.wealth.application.port.in;

import com.finflow.backend.finance.wealth.application.command.CreateWealthAccountCommand;

import com.finflow.backend.common.application.dto.UuidOutput;

public interface CreateWealthAccountPort {
    UuidOutput execute(CreateWealthAccountCommand command);
}
