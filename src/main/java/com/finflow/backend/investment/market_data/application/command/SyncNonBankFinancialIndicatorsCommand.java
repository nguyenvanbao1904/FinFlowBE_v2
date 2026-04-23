package com.finflow.backend.investment.market_data.application.command;

import com.finflow.backend.investment.market_data.application.dto.NonBankFinancialIndicatorRequestInput;

import java.util.List;

public record SyncNonBankFinancialIndicatorsCommand(
        List<NonBankFinancialIndicatorRequestInput> request
) {}
