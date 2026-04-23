package com.finflow.backend.investment.market_data.application.command;

import com.finflow.backend.investment.market_data.application.dto.CompanyRequestInput;

import java.util.List;

public record SyncCompaniesCommand(
        List<CompanyRequestInput> request
) {}
