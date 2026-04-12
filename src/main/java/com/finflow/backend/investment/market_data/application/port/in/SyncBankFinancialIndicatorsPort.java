package com.finflow.backend.investment.market_data.application.port.in;

import com.finflow.backend.investment.market_data.presentation.request.BankFinancialIndicatorRequestDTO;

import java.util.List;

public interface SyncBankFinancialIndicatorsPort {

    void execute(List<BankFinancialIndicatorRequestDTO> requestList);
}
