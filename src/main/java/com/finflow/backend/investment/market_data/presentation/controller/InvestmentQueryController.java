package com.finflow.backend.investment.market_data.presentation.controller;

import com.finflow.backend.common.versioning.ApiVersion;
import com.finflow.backend.investment.market_data.application.usecase.GetInvestmentDividendsUseCase;
import com.finflow.backend.investment.market_data.application.usecase.GetInvestmentFinancialSeriesUseCase;
import com.finflow.backend.investment.market_data.application.usecase.GetInvestmentFullAnalysisUseCase;
import com.finflow.backend.investment.market_data.application.usecase.GetInvestmentValuationsUseCase;
import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;
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

    private final GetInvestmentFullAnalysisUseCase fullAnalysisUseCase;
    private final GetInvestmentFinancialSeriesUseCase financialSeriesUseCase;
    private final GetInvestmentValuationsUseCase valuationsUseCase;
    private final GetInvestmentDividendsUseCase dividendsUseCase;

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
                fullAnalysisUseCase.execute(symbol, annualLimit, quarterlyLimit)
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
                financialSeriesUseCase.execute(symbol, annualLimit, quarterlyLimit)
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
                valuationsUseCase.execute(symbol, annualLimit, startDate, endDate, showQuarterly)
        );
    }

    @Operation(summary = "Get dividend chart series only")
    @GetMapping("/companies/{symbol}/analysis/dividends")
    public ResponseEntity<java.util.List<InvestmentAnalysisResponse.DividendPoint>> getCompanyDividends(
            @PathVariable String symbol,
            @Parameter(description = "Lọc theo tối đa N năm (theo ngày ghi nhận / quyền); bỏ qua = đủ lịch sử.")
            @RequestParam(required = false) Integer annualLimit
    ) {
        return ResponseEntity.ok(
                dividendsUseCase.execute(symbol, annualLimit)
        );
    }
}
