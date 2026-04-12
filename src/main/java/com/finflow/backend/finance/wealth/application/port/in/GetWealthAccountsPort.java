package com.finflow.backend.finance.wealth.application.port.in;

import java.util.List;
import com.finflow.backend.finance.wealth.presentation.response.WealthAccountResponse;

public interface GetWealthAccountsPort {
    List<WealthAccountResponse> execute(String userId);
}
