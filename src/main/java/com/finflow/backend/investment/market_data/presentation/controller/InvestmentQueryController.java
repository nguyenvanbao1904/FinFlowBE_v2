package com.finflow.backend.investment.market_data.presentation.controller;

import com.finflow.backend.common.versioning.ApiVersion;
import com.finflow.backend.investment.market_data.application.port.in.GetCompanyIndustriesPort;
import com.finflow.backend.investment.market_data.application.port.in.GetCompanyMarketDataPort;
import com.finflow.backend.investment.market_data.application.port.in.GetDailyValuationSeriesPort;
import com.finflow.backend.investment.market_data.application.port.in.GetIndustryNodesPort;
import com.finflow.backend.investment.market_data.application.port.in.GetInvestmentDividendsPort;
import com.finflow.backend.investment.market_data.application.port.in.GetInvestmentFinancialSeriesPort;
import com.finflow.backend.investment.market_data.application.port.in.GetInvestmentFullAnalysisPort;
import com.finflow.backend.investment.market_data.application.port.in.GetInvestmentValuationsPort;
import com.finflow.backend.investment.market_data.application.port.in.SuggestCompaniesPort;
import com.finflow.backend.investment.market_data.presentation.response.CompanyMarketDataResponse;
import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;
import com.finflow.backend.investment.market_data.presentation.response.CompanySuggestionResponse;
import com.finflow.backend.investment.market_data.presentation.response.CompanyIndustryResponse;
import com.finflow.backend.investment.market_data.presentation.response.IndustryNodeReadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/investments")
@RequiredArgsConstructor
@ApiVersion("1")
@Tag(name = "Investment", description = "Investment market analysis APIs")
public class InvestmentQueryController {

    private final GetInvestmentFullAnalysisPort fullAnalysisPort;
    private final GetInvestmentFinancialSeriesPort financialSeriesPort;
    private final GetInvestmentValuationsPort valuationsPort;
    private final GetDailyValuationSeriesPort dailyValuationSeriesPort;
    private final GetInvestmentDividendsPort dividendsPort;
    private final GetCompanyIndustriesPort getCompanyIndustriesPort;
    private final GetCompanyMarketDataPort getCompanyMarketDataPort;
    private final GetIndustryNodesPort getIndustryNodesPort;
    private final SuggestCompaniesPort suggestCompaniesPort;

    @Operation(summary = "Suggest companies by ticker prefix (fallback by companyName)")
    @GetMapping("/companies/suggest")
    public ResponseEntity<java.util.List<CompanySuggestionResponse>> suggestCompanies(
            @RequestParam(name = "q") String q,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(suggestCompaniesPort.execute(q, limit));
    }

    @Operation(summary = "Get industry labels for a list of company symbols")
    @GetMapping("/companies/industries")
    public ResponseEntity<java.util.List<CompanyIndustryResponse>> getCompanyIndustries(
            @RequestParam(name = "symbols") java.util.List<String> symbols
    ) {
        return ResponseEntity.ok(getCompanyIndustriesPort.execute(symbols));
    }

    @Operation(summary = "Get raw market-data sections for one company (AI/function-calling friendly)")
    @GetMapping("/companies/{symbol}/market-data")
    public ResponseEntity<CompanyMarketDataResponse> getCompanyMarketData(
            @PathVariable String symbol,
            @Parameter(
                    description = "Danh sách section cần lấy. Hỗ trợ: company, shareholders, dividends, "
                            + "financialIndicators, bankBalanceSheets, nonBankBalanceSheets, "
                            + "bankIncomeStatements, nonBankIncomeStatements, all. "
                            + "Có thể truyền nhiều lần hoặc comma-separated."
            )
            @RequestParam(name = "include", required = false) java.util.List<String> include,
            @Parameter(description = "Giới hạn dữ liệu theo năm cho sections dạng time-series.")
            @RequestParam(required = false) Integer annualLimit,
            @Parameter(description = "Giới hạn dữ liệu theo quý cho sections dạng time-series.")
            @RequestParam(required = false) Integer quarterlyLimit
    ) {
        return ResponseEntity.ok(getCompanyMarketDataPort.execute(symbol, include, annualLimit, quarterlyLimit));
    }

    @Operation(summary = "Get industry-node list (raw tree nodes)")
    @GetMapping("/industries/nodes")
    public ResponseEntity<java.util.List<IndustryNodeReadResponse>> getIndustryNodes() {
        return ResponseEntity.ok(getIndustryNodesPort.execute());
    }

    @Operation(summary = "Get stock analysis data for one company symbol")
    @GetMapping("/companies/{symbol}/analysis")
    public ResponseEntity<InvestmentAnalysisResponse> getCompanyAnalysis(
            @PathVariable String symbol,
            @Parameter(description = "Max số năm (điểm năm) cho biểu đồ; bỏ qua = đủ lịch sử. Response: thời gian tăng dần.")
            @RequestParam(required = false) Integer annualLimit,
            @Parameter(description = "Max số điểm quý (quarter > 0); bỏ qua = đủ lịch sử.")
            @RequestParam(required = false) Integer quarterlyLimit
    ) {
        return ResponseEntity.ok(
                fullAnalysisPort.execute(symbol, annualLimit, quarterlyLimit)
        );
    }

    @Operation(summary = "Get financial chart series only")
    @GetMapping("/companies/{symbol}/analysis/financials")
    public ResponseEntity<InvestmentAnalysisResponse.FinancialSeries> getCompanyFinancialSeries(
            @PathVariable String symbol,
            @Parameter(description = "Max số năm (điểm năm); bỏ qua = đủ lịch sử.")
            @RequestParam(required = false) Integer annualLimit,
            @Parameter(description = "Max số điểm quý; bỏ qua = đủ lịch sử.")
            @RequestParam(required = false) Integer quarterlyLimit
    ) {
        return ResponseEntity.ok(
                financialSeriesPort.execute(symbol, annualLimit, quarterlyLimit)
        );
    }

    @Operation(summary = "Get valuation chart series only")
    @GetMapping("/companies/{symbol}/analysis/valuations")
    public ResponseEntity<java.util.List<InvestmentAnalysisResponse.ValuationPoint>> getCompanyValuations(
            @PathVariable String symbol,
            @Parameter(description = "Lọc theo tối đa N năm có dữ liệu; bỏ qua = đủ lịch sử.")
            @RequestParam(required = false) Integer annualLimit,
            @Parameter(description = "Ngày bắt đầu (yyyy-MM-dd). Khi có, backend sẽ lọc valuations theo khoảng thay vì cắt ở FE.")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "Ngày kết thúc (yyyy-MM-dd). Khi có, backend sẽ lọc valuations theo khoảng thay vì cắt ở FE.")
            @RequestParam(required = false) String endDate,
            @Parameter(description = "Hiển thị theo quý (quarterly mode) hay theo năm (annual mode). Dùng chung với startDate/endDate.")
            @RequestParam(required = false) Boolean showQuarterly
    ) {
        return ResponseEntity.ok(
                valuationsPort.execute(symbol, annualLimit, startDate, endDate, showQuarterly)
        );
    }

    @Operation(summary = "Daily P/E–P/B–P/S series (Finfo close + fundamentals as-of each day)")
    @GetMapping("/companies/{symbol}/analysis/valuations/daily")
    public ResponseEntity<java.util.List<InvestmentAnalysisResponse.DailyValuationPoint>> getCompanyDailyValuations(
            @PathVariable String symbol,
            @Parameter(description = "yyyy-MM-dd", required = true)
            @RequestParam String startDate,
            @Parameter(description = "yyyy-MM-dd", required = true)
            @RequestParam String endDate
    ) {
        return ResponseEntity.ok(dailyValuationSeriesPort.execute(symbol, startDate, endDate));
    }

    @Operation(summary = "Get dividend chart series only")
    @GetMapping("/companies/{symbol}/analysis/dividends")
    public ResponseEntity<java.util.List<InvestmentAnalysisResponse.DividendPoint>> getCompanyDividends(
            @PathVariable String symbol,
            @Parameter(description = "Lọc theo tối đa N năm (theo ngày ghi nhận / quyền); bỏ qua = đủ lịch sử.")
            @RequestParam(required = false) Integer annualLimit
    ) {
        return ResponseEntity.ok(
                dividendsPort.execute(symbol, annualLimit)
        );
    }
}
