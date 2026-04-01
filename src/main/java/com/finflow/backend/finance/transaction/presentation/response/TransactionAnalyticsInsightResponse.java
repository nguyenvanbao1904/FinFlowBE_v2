package com.finflow.backend.finance.transaction.presentation.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAnalyticsInsightResponse {
    private String id;
    private String type;
    private String title;
    private String message;
    private Double confidence;
}

