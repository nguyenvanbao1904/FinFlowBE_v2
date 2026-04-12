package com.finflow.backend.investment.portfolio.application.port.in;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioResponse;

public interface GetPortfoliosPort {
    List<PortfolioResponse> execute(String userId);
}
