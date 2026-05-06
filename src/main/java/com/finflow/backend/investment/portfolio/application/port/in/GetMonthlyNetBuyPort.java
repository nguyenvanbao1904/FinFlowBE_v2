package com.finflow.backend.investment.portfolio.application.port.in;

import java.math.BigDecimal;
import java.time.YearMonth;

public interface GetMonthlyNetBuyPort {
    BigDecimal execute(String userId, YearMonth month);
}
