package com.finflow.backend.investment.market_data.application.command;

import com.finflow.backend.investment.market_data.application.dto.CompanyShareholderRequestInput;

import java.util.List;

public record SyncCompanyShareholdersCommand(
        String companyId,
        List<CompanyShareholderRequestInput> request
) {}
