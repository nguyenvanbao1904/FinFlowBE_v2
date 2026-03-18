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
@Table(name = "company_shareholders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyShareholder {

    @Id
    @Column(name = "id", nullable = false)
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "company_id", nullable = false, length = 10)
    private String companyId;

    @Column(name = "shareholder_name", nullable = false, length = 500)
    private String shareholderName;

    @Column(name = "quantity")
    private Long quantity;

    @Column(name = "share_own_percent", precision = 5, scale = 4)
    private BigDecimal shareOwnPercent;

    @Column(name = "update_date")
    private LocalDate updateDate;
}
