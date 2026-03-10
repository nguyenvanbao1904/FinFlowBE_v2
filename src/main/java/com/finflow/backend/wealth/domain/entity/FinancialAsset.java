package com.finflow.backend.wealth.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "financial_assets")
@PrimaryKeyJoinColumn(name = "asset_id")
public class FinancialAsset extends Asset {
    UUID walletId;
    String referenceCode;
}