package com.finflow.backend.investment.portfolio.application.port.out;

import com.finflow.backend.investment.portfolio.application.dto.PortfolioInsightItem;

import java.util.List;
import java.util.Map;

/**
 * Port for calling data_ai_service to generate portfolio insights.
 */
public interface DataAiPortfolioInsightsPort {

    /**
     * Sends a portfolio-insights request to data_ai_service and returns the parsed list of insights.
     *
     * @param payload JSON-serializable map containing portfolio data
     * @return list of insights (nhan_xet, canh_bao, loi_khuyen)
     */
    List<PortfolioInsightItem> fetchInsights(Map<String, Object> payload);
}
