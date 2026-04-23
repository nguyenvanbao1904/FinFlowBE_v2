package com.finflow.backend.investment.market_data.application.port.in;
import com.finflow.backend.investment.market_data.application.query.SuggestCompaniesQuery;

import com.finflow.backend.investment.market_data.application.dto.CompanySuggestionOutput;

import java.util.List;

public interface SuggestCompaniesPort {

    List<CompanySuggestionOutput> execute(SuggestCompaniesQuery query);
}
