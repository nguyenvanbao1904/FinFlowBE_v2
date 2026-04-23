package com.finflow.backend.investment.market_data.application.port.in;
import com.finflow.backend.investment.market_data.application.query.GetCompanyIndustriesQuery;

import com.finflow.backend.investment.market_data.application.dto.CompanyIndustryOutput;

import java.util.List;

public interface GetCompanyIndustriesPort {

    List<CompanyIndustryOutput> execute(GetCompanyIndustriesQuery query);
}
