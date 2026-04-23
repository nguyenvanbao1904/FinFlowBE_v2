package com.finflow.backend.finance.wealth.application.port.in;
import com.finflow.backend.finance.wealth.application.query.GetWealthAccountsQuery;

import java.util.List;
import com.finflow.backend.finance.wealth.application.dto.WealthAccountOutput;

public interface GetWealthAccountsPort {
    List<WealthAccountOutput> execute(GetWealthAccountsQuery query);
}
