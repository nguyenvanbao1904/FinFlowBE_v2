package com.finflow.backend.investment.portfolio.presentation.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PortfolioAssetResponse {

    String symbol;
    BigDecimal totalQuantity;
    BigDecimal averagePrice;
    /** Giá đóng cửa gần nhất (VND) từ VPS. Có thể null nếu không lấy được. */
    BigDecimal closePrice;
    /** Giá trị thị trường = totalQuantity × closePrice (VND). Null nếu thiếu closePrice. */
    BigDecimal marketValueClose;
    /** Lãi/lỗ tạm tính = (closePrice - averagePrice) × totalQuantity (VND). Null nếu thiếu closePrice. */
    BigDecimal unrealizedPnL;
    /** Lãi/lỗ tạm tính (%) = (closePrice/averagePrice - 1) × 100. Null nếu thiếu closePrice hoặc averagePrice=0. */
    BigDecimal unrealizedPnLPct;
    LocalDateTime updatedAt;
}

