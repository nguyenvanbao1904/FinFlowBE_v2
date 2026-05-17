package com.finflow.backend.investment.portfolio.presentation.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PortfolioInsightResponse {

    /** Category: nhan_xet, canh_bao, loi_khuyen */
    String category;
    /** Insight message in Vietnamese */
    String message;
}
