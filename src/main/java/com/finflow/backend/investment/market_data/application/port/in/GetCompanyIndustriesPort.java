package com.finflow.backend.investment.market_data.application.port.in;

import com.finflow.backend.investment.market_data.presentation.response.CompanyIndustryResponse;

import java.util.List;

public interface GetCompanyIndustriesPort {

    List<CompanyIndustryResponse> execute(List<String> symbols);
}
