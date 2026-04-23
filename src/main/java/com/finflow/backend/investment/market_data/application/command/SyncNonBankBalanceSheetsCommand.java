package com.finflow.backend.investment.market_data.application.command;

import com.finflow.backend.investment.market_data.application.dto.NonBankBalanceSheetRequestInput;

import java.util.List;

public record SyncNonBankBalanceSheetsCommand(
        List<NonBankBalanceSheetRequestInput> request
) {}
