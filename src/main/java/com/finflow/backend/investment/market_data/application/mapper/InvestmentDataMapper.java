package com.finflow.backend.investment.market_data.application.mapper;

import com.finflow.backend.investment.market_data.domain.entity.*;
import com.finflow.backend.investment.market_data.application.dto.*;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE,
        unmappedSourcePolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface InvestmentDataMapper {

    BankFinancialIndicator toBankEntity(BankFinancialIndicatorRequestInput dto);

    NonBankFinancialIndicator toNonBankEntity(NonBankFinancialIndicatorRequestInput dto);

    NonBankIncomeStatement toEntity(NonBankIncomeStatementRequestInput dto);

    BankIncomeStatement toEntity(BankIncomeStatementRequestInput dto);

    NonBankBalanceSheet toEntity(NonBankBalanceSheetRequestInput dto);

    BankBalanceSheet toEntity(BankBalanceSheetRequestInput dto);

    @BeanMapping(unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
    @Mapping(target = "industryNode", ignore = true)
    Company toEntity(CompanyRequestInput dto);

    CompanyShareholder toEntity(CompanyShareholderRequestInput dto);

    CompanyDividend toEntity(CompanyDividendRequestInput dto);

    CashFlowStatement toEntity(CashFlowStatementRequestInput dto);
}
