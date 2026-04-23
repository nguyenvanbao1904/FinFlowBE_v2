package com.finflow.backend.investment.market_data.application.command;

import com.finflow.backend.investment.market_data.application.dto.CompanyDividendRequestInput;

import java.util.List;

public record SyncCompanyDividendsCommand(
        String companyId,
        List<CompanyDividendRequestInput> request
) {}
