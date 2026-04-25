package com.finflow.backend.investment.market_data.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record NonBankIncomeStatementRequestDTO(
    @NotBlank(message = "REQUIRED_FIELD") String companyId,
    @NotNull(message = "REQUIRED_FIELD") Integer year,
    @NotNull(message = "REQUIRED_FIELD") Integer quarter,
    BigDecimal profitAfterTax,
    BigDecimal netRevenue,
    
    // --- DOANH THU CHI TIẾT (New Fields) ---
    BigDecimal totalRevenue,
    BigDecimal netProfit,
    BigDecimal grossProfit,
    BigDecimal costOfGoodsSold,
    BigDecimal sellingExpense,
    BigDecimal managingExpense
) {}
