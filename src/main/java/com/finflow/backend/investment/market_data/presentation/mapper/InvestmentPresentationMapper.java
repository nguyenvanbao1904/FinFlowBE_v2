package com.finflow.backend.investment.market_data.presentation.mapper;

import com.finflow.backend.investment.market_data.application.dto.*;
import com.finflow.backend.investment.market_data.presentation.request.*;
import com.finflow.backend.investment.market_data.presentation.response.*;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE,
        unmappedSourcePolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface InvestmentPresentationMapper {

    BankFinancialIndicatorRequestInput toInput(BankFinancialIndicatorRequestDTO dto);

    NonBankFinancialIndicatorRequestInput toInput(NonBankFinancialIndicatorRequestDTO dto);

    BankIncomeStatementRequestInput toInput(BankIncomeStatementRequestDTO dto);

    NonBankIncomeStatementRequestInput toInput(NonBankIncomeStatementRequestDTO dto);

    BankBalanceSheetRequestInput toInput(BankBalanceSheetRequestDTO dto);

    NonBankBalanceSheetRequestInput toInput(NonBankBalanceSheetRequestDTO dto);

    CompanyRequestInput toInput(CompanyRequestDTO dto);

    CompanyShareholderRequestInput toInput(CompanyShareholderRequestDTO dto);

    CompanyDividendRequestInput toInput(CompanyDividendRequestDTO dto);

    IndustryNodeRequestInput toInput(IndustryNodeRequestDTO dto);

    List<BankFinancialIndicatorRequestInput> toBankFinancialIndicatorInputs(List<BankFinancialIndicatorRequestDTO> dtos);

    List<NonBankFinancialIndicatorRequestInput> toNonBankFinancialIndicatorInputs(List<NonBankFinancialIndicatorRequestDTO> dtos);

    List<BankIncomeStatementRequestInput> toBankIncomeStatementInputs(List<BankIncomeStatementRequestDTO> dtos);

    List<NonBankIncomeStatementRequestInput> toNonBankIncomeStatementInputs(List<NonBankIncomeStatementRequestDTO> dtos);

    List<BankBalanceSheetRequestInput> toBankBalanceSheetInputs(List<BankBalanceSheetRequestDTO> dtos);

    List<NonBankBalanceSheetRequestInput> toNonBankBalanceSheetInputs(List<NonBankBalanceSheetRequestDTO> dtos);

    List<CompanyRequestInput> toCompanyInputs(List<CompanyRequestDTO> dtos);

    List<CompanyShareholderRequestInput> toCompanyShareholderInputs(List<CompanyShareholderRequestDTO> dtos);

    List<CompanyDividendRequestInput> toCompanyDividendInputs(List<CompanyDividendRequestDTO> dtos);

    List<IndustryNodeRequestInput> toIndustryNodeInputs(List<IndustryNodeRequestDTO> dtos);

    CashFlowStatementRequestInput toInput(CashFlowStatementRequestDTO dto);

    List<CashFlowStatementRequestInput> toCashFlowStatementInputs(List<CashFlowStatementRequestDTO> dtos);

    InvestmentAnalysisResponse toResponse(InvestmentAnalysisOutput output);

    InvestmentAnalysisResponse.FinancialSeries toResponse(InvestmentAnalysisOutput.FinancialSeries output);

    InvestmentAnalysisResponse.ValuationPoint toResponse(InvestmentAnalysisOutput.ValuationPoint output);

    InvestmentAnalysisResponse.DailyValuationPoint toResponse(InvestmentAnalysisOutput.DailyValuationPoint output);

    InvestmentAnalysisResponse.DividendPoint toResponse(InvestmentAnalysisOutput.DividendPoint output);

    List<InvestmentAnalysisResponse.ValuationPoint> toValuationResponses(List<InvestmentAnalysisOutput.ValuationPoint> output);

    List<InvestmentAnalysisResponse.DailyValuationPoint> toDailyValuationResponses(List<InvestmentAnalysisOutput.DailyValuationPoint> output);

    List<InvestmentAnalysisResponse.DividendPoint> toDividendResponses(List<InvestmentAnalysisOutput.DividendPoint> output);

    CompanyMarketDataResponse toResponse(CompanyMarketDataOutput output);

    CompanySuggestionResponse toResponse(CompanySuggestionOutput output);

    CompanyIndustryResponse toResponse(CompanyIndustryOutput output);

    IndustryNodeReadResponse toResponse(IndustryNodeReadOutput output);

    List<CompanySuggestionResponse> toCompanySuggestionResponses(List<CompanySuggestionOutput> output);

    List<CompanyIndustryResponse> toCompanyIndustryResponses(List<CompanyIndustryOutput> output);

    List<IndustryNodeReadResponse> toIndustryNodeReadResponses(List<IndustryNodeReadOutput> output);
}
