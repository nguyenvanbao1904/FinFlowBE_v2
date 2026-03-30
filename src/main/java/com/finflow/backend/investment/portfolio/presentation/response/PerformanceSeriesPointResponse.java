package com.finflow.backend.investment.portfolio.presentation.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PerformanceSeriesPointResponse(
        LocalDate date,
        BigDecimal value,
        /** % thay đổi so với điểm đầu tiên trong chuỗi (neo 0%). */
        BigDecimal returnPct
) {}
