package com.finflow.backend.investment.market_data.application.command;

import com.finflow.backend.investment.market_data.application.dto.BankIncomeStatementRequestInput;

import java.util.List;

public record SyncBankIncomeStatementsCommand(
        List<BankIncomeStatementRequestInput> request
) {}
