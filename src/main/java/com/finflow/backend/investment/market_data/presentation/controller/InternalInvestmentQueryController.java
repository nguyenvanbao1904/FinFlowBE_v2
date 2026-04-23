package com.finflow.backend.investment.market_data.presentation.controller;

import com.finflow.backend.investment.market_data.presentation.response.CompanyIndustryResponse;
import com.finflow.backend.investment.market_data.presentation.response.CompanyMarketDataResponse;
import com.finflow.backend.investment.market_data.presentation.response.CompanySuggestionResponse;
import com.finflow.backend.investment.market_data.presentation.response.IndustryNodeReadResponse;
import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/internal/investment/query")
@RequiredArgsConstructor
@Tag(name = "Internal Investment Query", description = "Internal market-data query APIs for AI orchestration")
public class InternalInvestmentQueryController {

    private final InvestmentQueryEndpointDelegate queryDelegate;

    @Operation(summary = "Suggest companies by ticker prefix (fallback by companyName)")
    @GetMapping("/companies/suggest")
    public ResponseEntity<List<CompanySuggestionResponse>> suggestCompanies(
            @RequestParam(name = "q") String q,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(queryDelegate.suggestCompanies(q, limit));
    }

    @Operation(summary = "Get industry labels for a list of company symbols")
    @GetMapping("/companies/industries")
    public ResponseEntity<List<CompanyIndustryResponse>> getCompanyIndustries(
            @RequestParam(name = "symbols") List<String> symbols
    ) {
        return ResponseEntity.ok(queryDelegate.getCompanyIndustries(symbols));
    }

    @Operation(summary = "Get raw market-data sections for one company (AI/function-calling friendly)")
    @GetMapping("/companies/{symbol}/market-data")
    public ResponseEntity<CompanyMarketDataResponse> getCompanyMarketData(
            @PathVariable String symbol,
            @Parameter(description = "Danh sách section cần lấy")
            @RequestParam(name = "include", required = false) List<String> include,
            @RequestParam(required = false) Integer annualLimit,
            @RequestParam(required = false) Integer quarterlyLimit
    ) {
        return ResponseEntity.ok(queryDelegate.getCompanyMarketData(symbol, include, annualLimit, quarterlyLimit));
    }

    @Operation(summary = "Get industry-node list (raw tree nodes)")
    @GetMapping("/industries/nodes")
    public ResponseEntity<List<IndustryNodeReadResponse>> getIndustryNodes() {
        return ResponseEntity.ok(queryDelegate.getIndustryNodes());
    }

    @Operation(summary = "Get stock analysis data for one company symbol")
    @GetMapping("/companies/{symbol}/analysis")
    public ResponseEntity<InvestmentAnalysisResponse> getCompanyAnalysis(
            @PathVariable String symbol,
            @RequestParam(required = false) Integer annualLimit,
            @RequestParam(required = false) Integer quarterlyLimit
    ) {
        return ResponseEntity.ok(queryDelegate.getCompanyAnalysis(symbol, annualLimit, quarterlyLimit));
    }

    @Operation(summary = "Get financial chart series only")
    @GetMapping("/companies/{symbol}/analysis/financials")
    public ResponseEntity<InvestmentAnalysisResponse.FinancialSeries> getCompanyFinancialSeries(
            @PathVariable String symbol,
            @RequestParam(required = false) Integer annualLimit,
            @RequestParam(required = false) Integer quarterlyLimit
    ) {
        return ResponseEntity.ok(queryDelegate.getCompanyFinancialSeries(symbol, annualLimit, quarterlyLimit));
    }

    @Operation(summary = "Get valuation chart series only")
    @GetMapping("/companies/{symbol}/analysis/valuations")
    public ResponseEntity<List<InvestmentAnalysisResponse.ValuationPoint>> getCompanyValuations(
            @PathVariable String symbol,
            @RequestParam(required = false) Integer annualLimit,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Boolean showQuarterly
    ) {
        return ResponseEntity.ok(queryDelegate.getCompanyValuations(symbol, annualLimit, startDate, endDate, showQuarterly));
    }

    @Operation(summary = "Daily P/E–P/B–P/S series")
    @GetMapping("/companies/{symbol}/analysis/valuations/daily")
    public ResponseEntity<List<InvestmentAnalysisResponse.DailyValuationPoint>> getCompanyDailyValuations(
            @PathVariable String symbol,
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        return ResponseEntity.ok(queryDelegate.getCompanyDailyValuations(symbol, startDate, endDate));
    }

    @Operation(summary = "Get dividend chart series only")
    @GetMapping("/companies/{symbol}/analysis/dividends")
    public ResponseEntity<List<InvestmentAnalysisResponse.DividendPoint>> getCompanyDividends(
            @PathVariable String symbol,
            @RequestParam(required = false) Integer annualLimit
    ) {
        return ResponseEntity.ok(queryDelegate.getCompanyDividends(symbol, annualLimit));
    }
}
