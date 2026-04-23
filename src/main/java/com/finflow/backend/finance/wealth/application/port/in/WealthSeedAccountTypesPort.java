package com.finflow.backend.finance.wealth.application.port.in;
import com.finflow.backend.finance.wealth.application.query.WealthSeedAccountTypesQuery;


public interface WealthSeedAccountTypesPort {
    void execute(WealthSeedAccountTypesQuery query);
}
