package com.finflow.backend.investment.market_data.application.command;

import com.finflow.backend.investment.market_data.application.dto.NonBankIncomeStatementRequestInput;

import java.util.List;

public record SyncNonBankIncomeStatementsCommand(
        List<NonBankIncomeStatementRequestInput> request
) {}
