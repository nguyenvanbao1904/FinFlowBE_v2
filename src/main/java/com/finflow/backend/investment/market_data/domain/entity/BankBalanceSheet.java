package com.finflow.backend.investment.market_data.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "bank_balance_sheets")
@DiscriminatorValue("BANK")
@Getter
@Setter
public class BankBalanceSheet extends BalanceSheet {
    
    // --- TÀI SẢN NGÂN HÀNG ---
    @Column(name = "balances_with_sbv", precision = 19, scale = 2)
    private BigDecimal balancesWithSbv; // Tiền gửi tại NHNN
    
    @Column(name = "interbank_placements_loans", precision = 19, scale = 2)
    private BigDecimal interbankPlacementsAndLoans; // Tiền gửi/Cho vay TCTD khác
    
    @Column(name = "trading_securities", precision = 19, scale = 2)
    private BigDecimal tradingSecurities; // Chứng khoán kinh doanh
    
    @Column(name = "investment_securities", precision = 19, scale = 2)
    private BigDecimal investmentSecurities; // Chứng khoán đầu tư
    
    @Column(name = "loans_to_customers", precision = 19, scale = 2)
    private BigDecimal loansToCustomers; // Cho vay khách hàng
    
    // --- NGUỒN VỐN NGÂN HÀNG ---
    @Column(name = "gov_sbv_debt", precision = 19, scale = 2)
    private BigDecimal govAndSbvDebt; // Nợ chính phủ và NHNN
    
    @Column(name = "deposits_borrowings_others", precision = 19, scale = 2)
    private BigDecimal depositsBorrowingsOthers; // Tiền gửi và vay TCTD khác
    
    @Column(name = "deposits_from_customers", precision = 19, scale = 2)
    private BigDecimal depositsFromCustomers; // Tiền gửi khách hàng
    
    @Column(name = "convertible_other_papers", precision = 19, scale = 2)
    private BigDecimal convertibleAndOtherPapers; // Phát hành giấy tờ có giá

    // --- NỢ CHI TIẾT ---
    @Column(name = "total_liabilities", precision = 19, scale = 2)
    private BigDecimal totalLiabilities; // NỢ PHẢI TRẢ (đồng)

    @Column(name = "customer_loan", precision = 20, scale = 2)
    private BigDecimal customerLoan;

    @Column(name = "standard_debt", precision = 20, scale = 2)
    private BigDecimal standardDebt;

    @Column(name = "watchlist_debt", precision = 20, scale = 2)
    private BigDecimal watchlistDebt;

    @Column(name = "substandard_debt", precision = 20, scale = 2)
    private BigDecimal substandardDebt;

    @Column(name = "doubtful_debt", precision = 20, scale = 2)
    private BigDecimal doubtfulDebt;

    @Column(name = "bad_debt", precision = 20, scale = 2)
    private BigDecimal badDebt;

    @Column(name = "provision_for_customer_loan_loss", precision = 20, scale = 2)
    private BigDecimal provisionForCustomerLoanLoss;

    @Column(name = "issuing_valuable_paper", precision = 20, scale = 2)
    private BigDecimal issuingValuablePaper;

    @Column(name = "total_equity", precision = 20, scale = 2)
    private BigDecimal totalEquity;
}
