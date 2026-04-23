package com.finflow.backend.finance.wealth.application.port.in;
import com.finflow.backend.finance.wealth.application.query.GetWealthAccountTypesQuery;

import java.util.List;
import com.finflow.backend.finance.wealth.application.dto.WealthAccountTypeOptionOutput;

public interface GetWealthAccountTypesPort {
    List<WealthAccountTypeOptionOutput> execute(GetWealthAccountTypesQuery query);
}
