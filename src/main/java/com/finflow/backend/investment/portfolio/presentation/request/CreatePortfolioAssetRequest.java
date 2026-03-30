package com.finflow.backend.investment.portfolio.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatePortfolioAssetRequest {

    @NotBlank(message = "PORTFOLIO_ASSET_SYMBOL_BLANK")
    String symbol;

    @NotNull(message = "PORTFOLIO_ASSET_QUANTITY_REQUIRED")
    BigDecimal quantity;

    @NotNull(message = "PORTFOLIO_ASSET_AVERAGE_PRICE_REQUIRED")
    BigDecimal averagePrice;
}

