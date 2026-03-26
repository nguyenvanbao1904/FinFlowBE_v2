package com.finflow.backend.investment.market_data.application.mapper;

import com.finflow.backend.investment.market_data.domain.entity.*;
import com.finflow.backend.investment.market_data.presentation.request.*;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE,
        unmappedSourcePolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface InvestmentDataMapper {

    BankFinancialIndicator toBankEntity(BankFinancialIndicatorRequestDTO dto);

    NonBankFinancialIndicator toNonBankEntity(NonBankFinancialIndicatorRequestDTO dto);

    NonBankIncomeStatement toEntity(NonBankIncomeStatementRequestDTO dto);

    BankIncomeStatement toEntity(BankIncomeStatementRequestDTO dto);

    NonBankBalanceSheet toEntity(NonBankBalanceSheetRequestDTO dto);

    BankBalanceSheet toEntity(BankBalanceSheetRequestDTO dto);

    @BeanMapping(unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
    @Mapping(target = "industryNode", ignore = true)
    Company toEntity(CompanyRequestDTO dto);

    CompanyShareholder toEntity(CompanyShareholderRequestDTO dto);

    CompanyDividend toEntity(CompanyDividendRequestDTO dto);
}
