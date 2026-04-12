package com.finflow.backend.investment.market_data.application.port.in;

import com.finflow.backend.investment.market_data.presentation.request.CompanyDividendRequestDTO;

import java.util.List;

public interface SyncCompanyDividendsPort {

    void execute(String companyId, List<CompanyDividendRequestDTO> request);
}
