package com.finflow.backend.investment.portfolio.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioInsightItem {

    private String category;  // nhan_xet, canh_bao, loi_khuyen
    private String message;
}
