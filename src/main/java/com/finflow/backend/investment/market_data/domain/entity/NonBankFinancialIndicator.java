package com.finflow.backend.investment.market_data.domain.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("NORMAL")
@Getter
@Setter
public class NonBankFinancialIndicator extends FinancialIndicator {
}
