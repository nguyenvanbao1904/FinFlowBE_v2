package com.finflow.backend.investment.market_data.application.port.in;

import com.finflow.backend.investment.market_data.presentation.request.BankBalanceSheetRequestDTO;

import java.util.List;

public interface SyncBankBalanceSheetsPort {

    void execute(List<BankBalanceSheetRequestDTO> requestList);
}
