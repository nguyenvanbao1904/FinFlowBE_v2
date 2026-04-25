package com.finflow.backend.investment.market_data.application.command;

import com.finflow.backend.investment.market_data.application.dto.CashFlowStatementRequestInput;

import java.util.List;

public record SyncCashFlowStatementsCommand(
        List<CashFlowStatementRequestInput> request
) {}
