package com.finflow.backend.finance.wealth.application.port.in;

import com.finflow.backend.finance.wealth.domain.entity.WealthAccount;
import java.math.BigDecimal;
import com.finflow.backend.finance.wealth.domain.entity.WealthAccountType;
import com.finflow.backend.finance.wealth.presentation.response.WealthAccountResponse;
import com.finflow.backend.finance.wealth.application.command.CreateWealthAccountCommand;

public interface CreateWealthAccountPort {
    WealthAccountResponse execute(CreateWealthAccountCommand command);
}
