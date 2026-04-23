package com.finflow.backend.investment.market_data.application.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record CompanyDividendRequestInput(
        String eventTitle,
        String eventType,
        String ratio,
        BigDecimal value,
        LocalDate recordDate,
        LocalDate exrightDate,
        LocalDate issueDate
) {}
