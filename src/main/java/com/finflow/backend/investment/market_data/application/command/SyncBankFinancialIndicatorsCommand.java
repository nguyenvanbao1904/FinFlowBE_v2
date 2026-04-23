package com.finflow.backend.investment.market_data.application.command;

import com.finflow.backend.investment.market_data.application.dto.BankFinancialIndicatorRequestInput;

import java.util.List;

public record SyncBankFinancialIndicatorsCommand(
        List<BankFinancialIndicatorRequestInput> request
) {}
