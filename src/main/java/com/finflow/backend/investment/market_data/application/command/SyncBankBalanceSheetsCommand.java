package com.finflow.backend.investment.market_data.application.command;

import com.finflow.backend.investment.market_data.application.dto.BankBalanceSheetRequestInput;

import java.util.List;

public record SyncBankBalanceSheetsCommand(
        List<BankBalanceSheetRequestInput> request
) {}
