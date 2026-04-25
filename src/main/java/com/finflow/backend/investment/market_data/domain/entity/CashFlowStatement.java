package com.finflow.backend.investment.market_data.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "cash_flow_statements")
@IdClass(CashFlowStatement.PK.class)
@Getter
@Setter
public class CashFlowStatement {

    @Id
    @Column(name = "company_id", nullable = false)
    private String companyId;

    @Id
    @Column(nullable = false)
    private int year;

    @Id
    @Column(nullable = false)
    private int quarter;

    @Column(name = "operating_cashflow", precision = 20, scale = 2)
    private BigDecimal operatingCashflow;

    @Column(name = "investing_cashflow", precision = 20, scale = 2)
    private BigDecimal investingCashflow;

    @Column(name = "financing_cashflow", precision = 20, scale = 2)
    private BigDecimal financingCashflow;

    @Getter
    @Setter
    public static class PK implements Serializable {
        private String companyId;
        private int year;
        private int quarter;
    }
}
