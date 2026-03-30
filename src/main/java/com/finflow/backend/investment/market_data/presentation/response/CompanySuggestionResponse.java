package com.finflow.backend.investment.market_data.presentation.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompanySuggestionResponse {
    String id; // ticker
    String companyName;
}

