package com.finflow.backend.finance.transaction.presentation.mapper;

import com.finflow.backend.finance.common.enums.CategoryType;
import com.finflow.backend.finance.transaction.application.dto.AnalyzeTransactionOutput;
import com.finflow.backend.finance.transaction.application.dto.AnalyticsInsightItem;
import com.finflow.backend.finance.transaction.application.dto.AnalyticsInsightsOutput;
import com.finflow.backend.finance.transaction.application.dto.CategoryOutput;
import com.finflow.backend.finance.transaction.application.dto.InternalTransactionUserContextOutput;
import com.finflow.backend.finance.transaction.application.dto.PersonalFinanceReportOutput;
import com.finflow.backend.finance.transaction.application.dto.TransactionChartOutput;
import com.finflow.backend.finance.transaction.application.dto.TransactionOutput;
import com.finflow.backend.finance.transaction.application.dto.TransactionSummaryOutput;
import com.finflow.backend.finance.transaction.presentation.response.AnalyzeTransactionResponse;
import com.finflow.backend.finance.transaction.presentation.response.CategoryResponse;
import com.finflow.backend.finance.transaction.presentation.response.InternalFinanceReportResponse;
import com.finflow.backend.finance.transaction.presentation.response.InternalUserContextResponse;
import com.finflow.backend.finance.transaction.presentation.response.TransactionAnalyticsInsightResponse;
import com.finflow.backend.finance.transaction.presentation.response.TransactionAnalyticsInsightsResponse;
import com.finflow.backend.finance.transaction.presentation.response.TransactionChartResponse;
import com.finflow.backend.finance.transaction.presentation.response.TransactionResponse;
import com.finflow.backend.finance.transaction.presentation.response.TransactionSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE,
        unmappedSourcePolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface TransactionPresentationMapper {

    @Mapping(target = "type", source = "type", qualifiedByName = "categoryTypeToString")
    CategoryResponse toResponse(CategoryOutput output);

    @Mapping(target = "type", source = "type", qualifiedByName = "categoryTypeToString")
    TransactionResponse toResponse(TransactionOutput output);

    TransactionSummaryResponse toResponse(TransactionSummaryOutput output);

    @Mapping(target = "type", source = "type", qualifiedByName = "categoryTypeToString")
    AnalyzeTransactionResponse toResponse(AnalyzeTransactionOutput output);

    TransactionChartResponse.ChartDataPoint toChartDataPoint(TransactionChartOutput.ChartPointOutput output);

    List<TransactionChartResponse.ChartDataPoint> toChartDataPoints(List<TransactionChartOutput.ChartPointOutput> outputs);

    TransactionAnalyticsInsightResponse toResponse(AnalyticsInsightItem output);

    List<TransactionAnalyticsInsightResponse> toInsightResponses(List<AnalyticsInsightItem> outputs);

    @Named("categoryTypeToString")
    default String categoryTypeToString(CategoryType type) {
        return type != null ? type.name() : null;
    }

    default TransactionChartResponse toResponse(TransactionChartOutput output) {
        return new TransactionChartResponse(
                toChartDataPoints(output.dataPoints()),
                output.periodLabel(),
                output.hasNext()
        );
    }

    default TransactionAnalyticsInsightsResponse toResponse(AnalyticsInsightsOutput output) {
        return TransactionAnalyticsInsightsResponse.builder()
                .insights(toInsightResponses(output.insights()))
                .cached(output.cached())
                .build();
    }

    // ── Internal API mappings ───────────────────────────────────────────

    InternalFinanceReportResponse toInternalFinanceReportResponse(PersonalFinanceReportOutput output);

    InternalFinanceReportResponse.Data toInternalData(PersonalFinanceReportOutput.Data data);

    InternalFinanceReportResponse.MonthlyPoint toInternalMonthlyPoint(PersonalFinanceReportOutput.MonthlyPoint point);

    InternalFinanceReportResponse.MonthTopCategory toInternalMonthTopCategory(PersonalFinanceReportOutput.MonthTopCategory cat);

    InternalFinanceReportResponse.TopExpenseCategory toInternalTopExpenseCategory(PersonalFinanceReportOutput.TopExpenseCategory cat);

    InternalFinanceReportResponse.SavingsRatePoint toInternalSavingsRatePoint(PersonalFinanceReportOutput.SavingsRatePoint point);

    InternalFinanceReportResponse.CategoryDelta toInternalCategoryDelta(PersonalFinanceReportOutput.CategoryDelta delta);

    InternalUserContextResponse toInternalUserContextResponse(InternalTransactionUserContextOutput output);

    InternalUserContextResponse.ContextCategory toInternalContextCategory(InternalTransactionUserContextOutput.ContextCategory cat);

    InternalUserContextResponse.ContextAccount toInternalContextAccount(InternalTransactionUserContextOutput.ContextAccount acc);
}
