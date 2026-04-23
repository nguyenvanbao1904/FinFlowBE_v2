package com.finflow.backend.finance.wealth.application.port.in;

import com.finflow.backend.finance.wealth.application.command.UpdateWealthAccountCommand;

import com.finflow.backend.common.application.dto.UuidOutput;

public interface UpdateWealthAccountPort {
    UuidOutput execute(UpdateWealthAccountCommand command);
}
