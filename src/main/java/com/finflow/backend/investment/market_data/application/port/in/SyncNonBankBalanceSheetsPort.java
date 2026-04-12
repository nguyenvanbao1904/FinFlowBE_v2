package com.finflow.backend.investment.market_data.application.port.in;

import com.finflow.backend.investment.market_data.presentation.request.NonBankBalanceSheetRequestDTO;

import java.util.List;

public interface SyncNonBankBalanceSheetsPort {

    void execute(List<NonBankBalanceSheetRequestDTO> requestList);
}
