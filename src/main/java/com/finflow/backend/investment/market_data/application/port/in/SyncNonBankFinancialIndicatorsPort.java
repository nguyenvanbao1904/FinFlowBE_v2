package com.finflow.backend.investment.market_data.application.port.in;

import com.finflow.backend.investment.market_data.presentation.request.NonBankFinancialIndicatorRequestDTO;

import java.util.List;

public interface SyncNonBankFinancialIndicatorsPort {

    void execute(List<NonBankFinancialIndicatorRequestDTO> requestList);
}
