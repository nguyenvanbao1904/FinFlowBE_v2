package com.finflow.backend.investment.portfolio.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "daily_portfolio_snapshots",
        uniqueConstraints = @UniqueConstraint(columnNames = {"portfolio_id", "snapshot_date"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DailyPortfolioSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "portfolio_id", nullable = false)
    UUID portfolioId;

    @Column(name = "snapshot_date", nullable = false)
    LocalDate snapshotDate;

    /** NAV = cash + mark-to-market stocks (VND). */
    @Column(name = "total_nav", nullable = false, precision = 24, scale = 2)
    BigDecimal totalNav;

    @Column(name = "cash_balance", nullable = false, precision = 19, scale = 2)
    BigDecimal cashBalance;
}
