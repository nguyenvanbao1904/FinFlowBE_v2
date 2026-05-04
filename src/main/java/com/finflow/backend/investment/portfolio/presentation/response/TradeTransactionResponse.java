package com.finflow.backend.investment.portfolio.presentation.response;

import com.finflow.backend.investment.portfolio.domain.entity.TradeType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TradeTransactionResponse(
    UUID id,
    TradeType tradeType,
    String symbol,
    BigDecimal quantity,
    BigDecimal price,
    BigDecimal totalAmount,
    BigDecimal feeAmount,
    BigDecimal taxAmount,
    LocalDateTime transactionDate
) {}
