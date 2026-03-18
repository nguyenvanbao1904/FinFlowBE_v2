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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "company_dividends")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDividend {

    @Id
    @Column(name = "id", nullable = false)
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "company_id", nullable = false, length = 10)
    private String companyId;

    @Column(name = "event_title", nullable = false, length = 1000)
    private String eventTitle;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType; // CASH or STOCK

    @Column(name = "ratio", length = 100)
    private String ratio;

    @Column(name = "value")
    private BigDecimal value;

    @Column(name = "record_date")
    private LocalDate recordDate;

    @Column(name = "exright_date")
    private LocalDate exrightDate;

    @Column(name = "issue_date")
    private LocalDate issueDate;
}
