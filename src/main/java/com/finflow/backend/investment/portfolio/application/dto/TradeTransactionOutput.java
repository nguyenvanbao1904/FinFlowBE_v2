package com.finflow.backend.investment.portfolio.application.dto;

import com.finflow.backend.investment.portfolio.domain.entity.TradeType;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record TradeTransactionOutput(
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
