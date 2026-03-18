package com.finflow.backend.investment.market_data.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "income_statements")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "company_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public abstract class IncomeStatement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private String companyId;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int quarter;

    // Chỉ tiêu chung (Bank hay Non-Bank đều có)
    @Column(name = "profit_after_tax", precision = 19, scale = 2)
    private BigDecimal profitAfterTax; // Lợi nhuận sau thuế của Cổ đông công ty mẹ
}
