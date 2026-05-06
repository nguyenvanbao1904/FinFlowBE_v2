package com.finflow.backend.investment.market_data.presentation.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FairValueResponse {
    String symbol;
    String companyName;
    int targetYear;
    String industryKey;
    String method;
    String weightsUsed;
    double priceComposite;
    double pricePE;
    double pricePB;
    double pricePS;
    double livePrice;
    double upsidePct;
    String verdict;
    double peTarget;
    double pbTarget;
    double cagr;
    String error;
}
