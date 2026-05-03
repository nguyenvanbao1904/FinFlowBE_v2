package com.finflow.backend.finance.wealth.presentation.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WealthAccountTypeOptionResponse {

    UUID id;
    String code;
    String displayName;
    String icon;
    String color;
    Boolean transactionEligible;
    /** True when balance is stored as negative (e.g. LOAN). */
    Boolean debt;
    /** Grouping key for UI display: LIQUID | INVESTMENT | ASSET | DEBT */
    String group;
}
