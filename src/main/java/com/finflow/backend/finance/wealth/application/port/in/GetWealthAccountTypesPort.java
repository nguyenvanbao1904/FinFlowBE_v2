package com.finflow.backend.finance.wealth.application.port.in;

import java.util.List;
import com.finflow.backend.finance.wealth.presentation.response.WealthAccountTypeOptionResponse;

public interface GetWealthAccountTypesPort {
    List<WealthAccountTypeOptionResponse> execute();
}
