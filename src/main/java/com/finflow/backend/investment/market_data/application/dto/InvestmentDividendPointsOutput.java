package com.finflow.backend.investment.market_data.application.dto;

import java.util.List;

public record InvestmentDividendPointsOutput(List<InvestmentAnalysisOutput.DividendPoint> points) {}
