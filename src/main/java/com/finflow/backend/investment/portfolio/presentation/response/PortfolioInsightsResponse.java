package com.finflow.backend.investment.portfolio.presentation.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PortfolioInsightsResponse {

    List<PortfolioInsightResponse> insights;
}
