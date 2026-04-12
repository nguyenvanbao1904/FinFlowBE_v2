package com.finflow.backend.investment.market_data.application.port.in;

import com.finflow.backend.investment.market_data.presentation.response.CompanySuggestionResponse;

import java.util.List;

public interface SuggestCompaniesPort {

    List<CompanySuggestionResponse> execute(String query, Integer limit);
}
