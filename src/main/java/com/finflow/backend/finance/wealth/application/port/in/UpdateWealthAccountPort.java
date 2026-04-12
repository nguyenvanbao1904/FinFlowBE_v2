package com.finflow.backend.finance.wealth.application.port.in;

import com.finflow.backend.finance.wealth.domain.entity.WealthAccount;
import java.math.BigDecimal;
import com.finflow.backend.finance.wealth.application.command.UpdateWealthAccountCommand;
import com.finflow.backend.finance.wealth.domain.entity.WealthAccountType;
import com.finflow.backend.finance.wealth.presentation.response.WealthAccountResponse;
import java.util.UUID;

public interface UpdateWealthAccountPort {
    WealthAccountResponse execute(UpdateWealthAccountCommand command);
}
