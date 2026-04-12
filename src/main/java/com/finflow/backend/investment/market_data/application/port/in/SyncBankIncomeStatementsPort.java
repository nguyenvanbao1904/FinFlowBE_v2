package com.finflow.backend.investment.market_data.application.port.in;

import com.finflow.backend.investment.market_data.presentation.request.BankIncomeStatementRequestDTO;

import java.util.List;

public interface SyncBankIncomeStatementsPort {

    void execute(List<BankIncomeStatementRequestDTO> requestList);
}
