package com.finflow.backend.investment.market_data.application.mapper;

import com.finflow.backend.investment.market_data.domain.entity.CompanyDividend;
import com.finflow.backend.investment.market_data.domain.entity.CompanyShareholder;
import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.time.LocalDate;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface InvestmentAnalysisPointMapper {

    InvestmentAnalysisResponse.ValuationPoint toValuationPoint(FinancialIndicator indicator);

    InvestmentAnalysisResponse.DividendPoint toDividendPoint(CompanyDividend dividend);

    @Mapping(source = "shareholderName", target = "name")
    @Mapping(source = "shareOwnPercent", target = "percentage")
    InvestmentAnalysisResponse.ShareholderPoint toShareholderPoint(CompanyShareholder shareholder);

    // --- Type conversions (MapStruct uses these for implicit conversions) ---
    default Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    default Double toDouble(Long value) {
        return value == null ? null : value.doubleValue();
    }

    default String localDateToString(LocalDate value) {
        return value == null ? null : value.toString();
    }
}
