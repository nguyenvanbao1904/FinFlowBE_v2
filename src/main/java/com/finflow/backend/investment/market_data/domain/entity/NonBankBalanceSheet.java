package com.finflow.backend.investment.market_data.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "non_bank_balance_sheets")
@DiscriminatorValue("NON_BANK")
@Getter
@Setter
public class NonBankBalanceSheet extends BalanceSheet {

    // --- TÀI SẢN DOANH NGHIỆP ---
    @Column(name = "short_term_investments", precision = 19, scale = 2)
    private BigDecimal shortTermInvestments; // Đầu tư ngắn hạn
    
    @Column(name = "short_term_receivables", precision = 19, scale = 2)
    private BigDecimal shortTermReceivables; // Phải thu ngắn hạn
    
    @Column(name = "long_term_receivables", precision = 19, scale = 2)
    private BigDecimal longTermReceivables; // Phải thu dài hạn
    
    @Column(name = "inventories", precision = 19, scale = 2)
    private BigDecimal inventories; // Hàng tồn kho
    
    @Column(name = "fixed_assets", precision = 19, scale = 2)
    private BigDecimal fixedAssets; // Tài sản cố định

    // --- NGUỒN VỐN DOANH NGHIỆP ---
    @Column(name = "short_term_borrowings", precision = 19, scale = 2)
    private BigDecimal shortTermBorrowings; // Vay ngắn hạn
    
    @Column(name = "long_term_borrowings", precision = 19, scale = 2)
    private BigDecimal longTermBorrowings; // Vay dài hạn
    
    @Column(name = "advances_from_customers", precision = 19, scale = 2)
    private BigDecimal advancesFromCustomers; // Người mua trả tiền trước

    // --- NỢ PHẢI TRẢ (GỌI CHUNG) ---
    @Column(name = "total_liabilities", precision = 19, scale = 2)
    private BigDecimal totalLiabilities; // NỢ PHẢI TRẢ (đồng)
}
