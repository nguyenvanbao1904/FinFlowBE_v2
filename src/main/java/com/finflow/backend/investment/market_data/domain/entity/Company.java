package com.finflow.backend.investment.market_data.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(name = "industry", length = 255)
    private String industry;

    @Column(name = "company_name", length = 500)
    private String companyName;

    @Column(name = "company_type", nullable = false, length = 50)
    private String companyType; // BANK or NON_BANK

}
