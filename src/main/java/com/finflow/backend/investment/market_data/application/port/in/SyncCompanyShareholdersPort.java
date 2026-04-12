package com.finflow.backend.investment.market_data.application.port.in;

import com.finflow.backend.investment.market_data.presentation.request.CompanyShareholderRequestDTO;

import java.util.List;

public interface SyncCompanyShareholdersPort {

    void execute(String companyId, List<CompanyShareholderRequestDTO> request);
}
