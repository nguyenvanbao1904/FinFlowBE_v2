package com.finflow.backend.investment.portfolio.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "daily_market_index_snapshots",
        uniqueConstraints = @UniqueConstraint(columnNames = {"code", "snapshot_date"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DailyMarketIndexSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false, length = 32)
    String code;

    @Column(name = "snapshot_date", nullable = false)
    LocalDate snapshotDate;

    @Column(nullable = false, precision = 20, scale = 4)
    BigDecimal close;
}
