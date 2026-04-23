package com.finflow.backend.investment.market_data.presentation.controller;

import com.finflow.backend.investment.market_data.application.port.in.GetCompanyIndustriesPort;
import com.finflow.backend.investment.market_data.application.port.in.GetCompanyMarketDataPort;
import com.finflow.backend.investment.market_data.application.port.in.GetDailyValuationSeriesPort;
import com.finflow.backend.investment.market_data.application.port.in.GetIndustryNodesPort;
import com.finflow.backend.investment.market_data.application.port.in.GetInvestmentDividendsPort;
import com.finflow.backend.investment.market_data.application.port.in.GetInvestmentFinancialSeriesPort;
import com.finflow.backend.investment.market_data.application.port.in.GetInvestmentFullAnalysisPort;
import com.finflow.backend.investment.market_data.application.port.in.GetInvestmentValuationsPort;
import com.finflow.backend.investment.market_data.application.port.in.SuggestCompaniesPort;
import com.finflow.backend.investment.market_data.application.query.GetCompanyIndustriesQuery;
import com.finflow.backend.investment.market_data.application.query.GetCompanyMarketDataQuery;
import com.finflow.backend.investment.market_data.application.query.GetDailyValuationSeriesQuery;
import com.finflow.backend.investment.market_data.application.query.GetIndustryNodesQuery;
import com.finflow.backend.investment.market_data.application.query.GetInvestmentDividendsQuery;
import com.finflow.backend.investment.market_data.application.query.GetInvestmentFinancialSeriesQuery;
import com.finflow.backend.investment.market_data.application.query.GetInvestmentFullAnalysisQuery;
import com.finflow.backend.investment.market_data.application.query.GetInvestmentValuationsQuery;
import com.finflow.backend.investment.market_data.application.query.SuggestCompaniesQuery;
import com.finflow.backend.investment.market_data.presentation.mapper.InvestmentPresentationMapper;
import com.finflow.backend.investment.market_data.presentation.response.CompanyIndustryResponse;
import com.finflow.backend.investment.market_data.presentation.response.CompanyMarketDataResponse;
import com.finflow.backend.investment.market_data.presentation.response.CompanySuggestionResponse;
import com.finflow.backend.investment.market_data.presentation.response.IndustryNodeReadResponse;
import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Shared query endpoint logic used by both public and internal controllers.
 */
@Component
@RequiredArgsConstructor
public class InvestmentQueryEndpointDelegate {

    private final GetInvestmentFullAnalysisPort fullAnalysisPort;
    private final GetInvestmentFinancialSeriesPort financialSeriesPort;
    private final GetInvestmentValuationsPort valuationsPort;
    private final GetDailyValuationSeriesPort dailyValuationSeriesPort;
    private final GetInvestmentDividendsPort dividendsPort;
    private final GetCompanyIndustriesPort getCompanyIndustriesPort;
    private final GetCompanyMarketDataPort getCompanyMarketDataPort;
    private final GetIndustryNodesPort getIndustryNodesPort;
    private final SuggestCompaniesPort suggestCompaniesPort;
    private final InvestmentPresentationMapper presentationMapper;

    public List<CompanySuggestionResponse> suggestCompanies(String q, Integer limit) {
        return presentationMapper.toCompanySuggestionResponses(suggestCompaniesPort.execute(new SuggestCompaniesQuery(q, limit)));
    }

    public List<CompanyIndustryResponse> getCompanyIndustries(List<String> symbols) {
        return presentationMapper.toCompanyIndustryResponses(getCompanyIndustriesPort.execute(new GetCompanyIndustriesQuery(symbols)));
    }

    public CompanyMarketDataResponse getCompanyMarketData(String symbol, List<String> include, Integer annualLimit, Integer quarterlyLimit) {
        return presentationMapper.toResponse(
                getCompanyMarketDataPort.execute(new GetCompanyMarketDataQuery(symbol, include, annualLimit, quarterlyLimit))
        );
    }

    public List<IndustryNodeReadResponse> getIndustryNodes() {
        return presentationMapper.toIndustryNodeReadResponses(getIndustryNodesPort.execute(new GetIndustryNodesQuery()));
    }

    public InvestmentAnalysisResponse getCompanyAnalysis(String symbol, Integer annualLimit, Integer quarterlyLimit) {
        return presentationMapper.toResponse(fullAnalysisPort.execute(new GetInvestmentFullAnalysisQuery(symbol, annualLimit, quarterlyLimit)));
    }

    public InvestmentAnalysisResponse.FinancialSeries getCompanyFinancialSeries(String symbol, Integer annualLimit, Integer quarterlyLimit) {
        return presentationMapper.toResponse(
                financialSeriesPort.execute(new GetInvestmentFinancialSeriesQuery(symbol, annualLimit, quarterlyLimit)).series()
        );
    }

    public List<InvestmentAnalysisResponse.ValuationPoint> getCompanyValuations(
            String symbol,
            Integer annualLimit,
            String startDate,
            String endDate,
            Boolean showQuarterly
    ) {
        return presentationMapper.toValuationResponses(
                valuationsPort.execute(new GetInvestmentValuationsQuery(symbol, annualLimit, startDate, endDate, showQuarterly)).points()
        );
    }

    public List<InvestmentAnalysisResponse.DailyValuationPoint> getCompanyDailyValuations(String symbol, String startDate, String endDate) {
        return presentationMapper.toDailyValuationResponses(
                dailyValuationSeriesPort.execute(new GetDailyValuationSeriesQuery(symbol, startDate, endDate)).points()
        );
    }

    public List<InvestmentAnalysisResponse.DividendPoint> getCompanyDividends(String symbol, Integer annualLimit) {
        return presentationMapper.toDividendResponses(dividendsPort.execute(new GetInvestmentDividendsQuery(symbol, annualLimit)).points());
    }
}
