package com.finflow.backend.investment.market_data.application.mapper;

import com.finflow.backend.investment.market_data.domain.entity.*;
import com.finflow.backend.investment.market_data.presentation.request.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface InvestmentDataMapper {

    FinancialIndicator toEntity(FinancialIndicatorRequestDTO dto);
    
    NonBankIncomeStatement toEntity(NonBankIncomeStatementRequestDTO dto);
    
    BankIncomeStatement toEntity(BankIncomeStatementRequestDTO dto);
    
    NonBankBalanceSheet toEntity(NonBankBalanceSheetRequestDTO dto);
    
    BankBalanceSheet toEntity(BankBalanceSheetRequestDTO dto);

    Company toEntity(CompanyRequestDTO dto);

    CompanyShareholder toEntity(CompanyShareholderRequestDTO dto);

    CompanyDividend toEntity(CompanyDividendRequestDTO dto);
}
