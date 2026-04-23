package com.finflow.backend.investment.market_data.presentation.controller;

import com.finflow.backend.investment.market_data.application.port.in.SyncBankBalanceSheetsPort;
import com.finflow.backend.investment.market_data.application.port.in.SyncBankFinancialIndicatorsPort;
import com.finflow.backend.investment.market_data.application.port.in.SyncBankIncomeStatementsPort;
import com.finflow.backend.investment.market_data.application.port.in.SyncCompaniesPort;
import com.finflow.backend.investment.market_data.application.port.in.SyncCompanyDividendsPort;
import com.finflow.backend.investment.market_data.application.port.in.SyncCompanyShareholdersPort;
import com.finflow.backend.investment.market_data.application.command.SyncCompanyDividendsCommand;
import com.finflow.backend.investment.market_data.application.command.SyncCompaniesCommand;
import com.finflow.backend.investment.market_data.application.command.SyncCompanyShareholdersCommand;
import com.finflow.backend.investment.market_data.application.command.SyncIndustryNodesCommand;
import com.finflow.backend.investment.market_data.application.command.SyncBankFinancialIndicatorsCommand;
import com.finflow.backend.investment.market_data.application.command.SyncNonBankFinancialIndicatorsCommand;
import com.finflow.backend.investment.market_data.application.command.SyncBankIncomeStatementsCommand;
import com.finflow.backend.investment.market_data.application.command.SyncNonBankIncomeStatementsCommand;
import com.finflow.backend.investment.market_data.application.command.SyncBankBalanceSheetsCommand;
import com.finflow.backend.investment.market_data.application.command.SyncNonBankBalanceSheetsCommand;
import com.finflow.backend.investment.market_data.application.port.in.SyncIndustryNodesPort;
import com.finflow.backend.investment.market_data.application.port.in.SyncNonBankBalanceSheetsPort;
import com.finflow.backend.investment.market_data.application.port.in.SyncNonBankFinancialIndicatorsPort;
import com.finflow.backend.investment.market_data.application.port.in.SyncNonBankIncomeStatementsPort;
import com.finflow.backend.investment.market_data.presentation.mapper.InvestmentPresentationMapper;
import com.finflow.backend.investment.market_data.presentation.request.BankBalanceSheetRequestDTO;
import com.finflow.backend.investment.market_data.presentation.request.BankFinancialIndicatorRequestDTO;
import com.finflow.backend.investment.market_data.presentation.request.BankIncomeStatementRequestDTO;
import com.finflow.backend.investment.market_data.presentation.request.CompanyDividendRequestDTO;
import com.finflow.backend.investment.market_data.presentation.request.IndustryNodeRequestDTO;
import com.finflow.backend.investment.market_data.presentation.request.CompanyRequestDTO;
import com.finflow.backend.investment.market_data.presentation.request.CompanyShareholderRequestDTO;
import com.finflow.backend.investment.market_data.presentation.request.NonBankBalanceSheetRequestDTO;
import com.finflow.backend.investment.market_data.presentation.request.NonBankFinancialIndicatorRequestDTO;
import com.finflow.backend.investment.market_data.presentation.request.NonBankIncomeStatementRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/internal/investment/sync")
@RequiredArgsConstructor
public class InternalInvestmentSyncController {
    private final SyncBankIncomeStatementsPort syncBankIncomeStatementsPort;
    private final SyncNonBankIncomeStatementsPort syncNonBankIncomeStatementsPort;
    private final SyncBankBalanceSheetsPort syncBankBalanceSheetsPort;
    private final SyncNonBankBalanceSheetsPort syncNonBankBalanceSheetsPort;
    private final SyncBankFinancialIndicatorsPort syncBankFinancialIndicatorsPort;
    private final SyncNonBankFinancialIndicatorsPort syncNonBankFinancialIndicatorsPort;

    private final SyncCompaniesPort syncCompaniesPort;
    private final SyncCompanyShareholdersPort syncCompanyShareholdersPort;
    private final SyncCompanyDividendsPort syncCompanyDividendsPort;
    private final SyncIndustryNodesPort syncIndustryNodesPort;
    private final InvestmentPresentationMapper presentationMapper;

    @Operation(summary = "Sync industry nodes")
    @PostMapping("/industry-nodes")
    public ResponseEntity<Void> syncIndustryNodes(@RequestBody @Valid List<IndustryNodeRequestDTO> request) {
        log.info("Received sync request for {} industry tree nodes", request.size());
        syncIndustryNodesPort.execute(new SyncIndustryNodesCommand(presentationMapper.toIndustryNodeInputs(request)));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Sync bank financial indicators")
    @PostMapping("/bank-financial-indicators")
    public ResponseEntity<Void> syncBankFinancialIndicators(@RequestBody @Valid List<BankFinancialIndicatorRequestDTO> request) {
        log.info("Received sync request for {} bank financial indicators", request.size());
        syncBankFinancialIndicatorsPort.execute(new SyncBankFinancialIndicatorsCommand(presentationMapper.toBankFinancialIndicatorInputs(request)));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Sync non-bank financial indicators")
    @PostMapping("/non-bank-financial-indicators")
    public ResponseEntity<Void> syncNonBankFinancialIndicators(@RequestBody @Valid List<NonBankFinancialIndicatorRequestDTO> request) {
        log.info("Received sync request for {} non-bank financial indicators", request.size());
        syncNonBankFinancialIndicatorsPort.execute(new SyncNonBankFinancialIndicatorsCommand(presentationMapper.toNonBankFinancialIndicatorInputs(request)));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Sync bank income statements")
    @PostMapping("/bank-income-statements")
    public ResponseEntity<Void> syncBankIncomeStatements(@RequestBody @Valid List<BankIncomeStatementRequestDTO> request) {
        log.info("Received sync request for {} bank income statements", request.size());
        syncBankIncomeStatementsPort.execute(new SyncBankIncomeStatementsCommand(presentationMapper.toBankIncomeStatementInputs(request)));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Sync non-bank income statements")
    @PostMapping("/non-bank-income-statements")
    public ResponseEntity<Void> syncNonBankIncomeStatements(@RequestBody @Valid List<NonBankIncomeStatementRequestDTO> request) {
        log.info("Received sync request for {} non-bank income statements", request.size());
        syncNonBankIncomeStatementsPort.execute(new SyncNonBankIncomeStatementsCommand(presentationMapper.toNonBankIncomeStatementInputs(request)));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Sync bank balance sheets")
    @PostMapping("/bank-balance-sheets")
    public ResponseEntity<Void> syncBankBalanceSheets(@RequestBody @Valid List<BankBalanceSheetRequestDTO> request) {
        log.info("Received sync request for {} bank balance sheets", request.size());
        syncBankBalanceSheetsPort.execute(new SyncBankBalanceSheetsCommand(presentationMapper.toBankBalanceSheetInputs(request)));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Sync non-bank balance sheets")
    @PostMapping("/non-bank-balance-sheets")
    public ResponseEntity<Void> syncNonBankBalanceSheets(@RequestBody @Valid List<NonBankBalanceSheetRequestDTO> request) {
        log.info("Received sync request for {} non-bank balance sheets", request.size());
        syncNonBankBalanceSheetsPort.execute(new SyncNonBankBalanceSheetsCommand(presentationMapper.toNonBankBalanceSheetInputs(request)));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Sync companies data")
    @PostMapping("/companies")
    public ResponseEntity<Void> syncCompanies(@RequestBody @Valid List<CompanyRequestDTO> request) {
        syncCompaniesPort.execute(new SyncCompaniesCommand(presentationMapper.toCompanyInputs(request)));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Sync company shareholders")
    @PostMapping("/shareholders/{companyId}")
    public ResponseEntity<Void> syncShareholders(@PathVariable String companyId, @RequestBody @Valid List<CompanyShareholderRequestDTO> request) {
        syncCompanyShareholdersPort.execute(new SyncCompanyShareholdersCommand(companyId, presentationMapper.toCompanyShareholderInputs(request)));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Sync company dividends")
    @PostMapping("/dividends/{companyId}")
    public ResponseEntity<Void> syncDividends(@PathVariable String companyId, @RequestBody @Valid List<CompanyDividendRequestDTO> request) {
        syncCompanyDividendsPort.execute(new SyncCompanyDividendsCommand(companyId, presentationMapper.toCompanyDividendInputs(request)));
        return ResponseEntity.ok().build();
    }
}
