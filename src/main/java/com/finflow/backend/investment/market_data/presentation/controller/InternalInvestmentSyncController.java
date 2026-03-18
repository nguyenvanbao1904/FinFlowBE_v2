package com.finflow.backend.investment.market_data.presentation.controller;

import com.finflow.backend.investment.market_data.application.usecase.*;
import com.finflow.backend.investment.market_data.presentation.request.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/internal/investment/sync")
@RequiredArgsConstructor
public class InternalInvestmentSyncController {

    private final SyncFinancialIndicatorsUseCase syncFinancialIndicatorsUseCase;
    private final SyncBankIncomeStatementsUseCase syncBankIncomeStatementsUseCase;
    private final SyncNonBankIncomeStatementsUseCase syncNonBankIncomeStatementsUseCase;
    private final SyncBankBalanceSheetsUseCase syncBankBalanceSheetsUseCase;
    private final SyncNonBankBalanceSheetsUseCase syncNonBankBalanceSheetsUseCase;

    private final SyncCompaniesUseCase syncCompaniesUseCase;
    private final SyncCompanyShareholdersUseCase syncCompanyShareholdersUseCase;
    private final SyncCompanyDividendsUseCase syncCompanyDividendsUseCase;

    @PostMapping("/financial-indicators")
    public ResponseEntity<Void> syncFinancialIndicators(@RequestBody @Valid List<FinancialIndicatorRequestDTO> request) {
        log.info("Received sync request for {} financial indicators", request.size());
        syncFinancialIndicatorsUseCase.execute(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bank-income-statements")
    public ResponseEntity<Void> syncBankIncomeStatements(@RequestBody @Valid List<BankIncomeStatementRequestDTO> request) {
        log.info("Received sync request for {} bank income statements", request.size());
        syncBankIncomeStatementsUseCase.execute(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/non-bank-income-statements")
    public ResponseEntity<Void> syncNonBankIncomeStatements(@RequestBody @Valid List<NonBankIncomeStatementRequestDTO> request) {
        log.info("Received sync request for {} non-bank income statements", request.size());
        syncNonBankIncomeStatementsUseCase.execute(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bank-balance-sheets")
    public ResponseEntity<Void> syncBankBalanceSheets(@RequestBody @Valid List<BankBalanceSheetRequestDTO> request) {
        log.info("Received sync request for {} bank balance sheets", request.size());
        syncBankBalanceSheetsUseCase.execute(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/non-bank-balance-sheets")
    public ResponseEntity<Void> syncNonBankBalanceSheets(@RequestBody @Valid List<NonBankBalanceSheetRequestDTO> request) {
        log.info("Received sync request for {} non-bank balance sheets", request.size());
        syncNonBankBalanceSheetsUseCase.execute(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/companies")
    public ResponseEntity<Void> syncCompanies(@RequestBody @Valid List<CompanyRequestDTO> request) {
        syncCompaniesUseCase.execute(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/shareholders/{companyId}")
    public ResponseEntity<Void> syncShareholders(@PathVariable String companyId, @RequestBody @Valid List<CompanyShareholderRequestDTO> request) {
        syncCompanyShareholdersUseCase.execute(companyId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/dividends/{companyId}")
    public ResponseEntity<Void> syncDividends(@PathVariable String companyId, @RequestBody @Valid List<CompanyDividendRequestDTO> request) {
        syncCompanyDividendsUseCase.execute(companyId, request);
        return ResponseEntity.ok().build();
    }
}
