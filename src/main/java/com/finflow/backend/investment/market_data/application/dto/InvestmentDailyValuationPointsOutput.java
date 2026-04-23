package com.finflow.backend.investment.market_data.application.dto;

import java.util.List;

public record InvestmentDailyValuationPointsOutput(List<InvestmentAnalysisOutput.DailyValuationPoint> points) {}
