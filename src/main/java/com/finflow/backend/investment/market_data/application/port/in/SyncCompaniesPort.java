package com.finflow.backend.investment.market_data.application.port.in;

import com.finflow.backend.investment.market_data.presentation.request.CompanyRequestDTO;

import java.util.List;

public interface SyncCompaniesPort {

    void execute(List<CompanyRequestDTO> requests);
}
