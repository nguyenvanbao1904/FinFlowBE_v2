package com.finflow.backend.investment.market_data.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Công ty gán vào <strong>nút lá</strong> trên cây {@link IndustryNode} (độ sâu tuỳ nhánh).
 * Mã ICB hiển thị lấy từ {@code industryNode.icbCode} khi cần API public.
 */
@Entity
@Table(name = "companies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    @Id
    @Column(name = "id", nullable = false, length = 10)
    private String id; // Ticker Symbol (e.g., FPT)

    @Column(name = "exchange", nullable = false, length = 50)
    private String exchange; // HOSE, HNX, UPCOM

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "industry_node_id")
    private IndustryNode industryNode;

    @Column(name = "company_name", length = 500)
    private String companyName;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "company_type", nullable = false, length = 50)
    private String companyType; // BANK or NON_BANK

}
