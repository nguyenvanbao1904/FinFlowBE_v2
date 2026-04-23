package com.finflow.backend.investment.market_data.domain.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("BANK")
@Getter
@Setter
public class BankFinancialIndicator extends FinancialIndicator {
}
