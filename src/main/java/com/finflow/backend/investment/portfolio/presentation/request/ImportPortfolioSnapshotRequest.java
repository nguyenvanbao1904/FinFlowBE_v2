package com.finflow.backend.investment.portfolio.presentation.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ImportPortfolioSnapshotRequest {

    // snapshot should overwrite current cashBalance
    @NotNull(message = "CASH_BALANCE_REQUIRED")
    BigDecimal cashBalance;

    // optional: allow empty holdings (clears all assets)
    @Valid
    List<HoldingSnapshotRequest> holdings;

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class HoldingSnapshotRequest {

        String symbol;

        @NotNull(message = "HOLDING_QUANTITY_REQUIRED")
        BigDecimal totalQuantity;

        @NotNull(message = "HOLDING_AVERAGE_PRICE_REQUIRED")
        BigDecimal averagePrice;
    }
}

