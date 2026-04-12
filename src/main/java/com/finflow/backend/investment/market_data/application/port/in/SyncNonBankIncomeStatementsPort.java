package com.finflow.backend.investment.market_data.application.port.in;

import com.finflow.backend.investment.market_data.presentation.request.NonBankIncomeStatementRequestDTO;

import java.util.List;

public interface SyncNonBankIncomeStatementsPort {

    void execute(List<NonBankIncomeStatementRequestDTO> requestList);
}
