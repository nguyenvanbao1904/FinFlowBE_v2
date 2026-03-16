package com.finflow.backend.finance.wealth.presentation.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WealthAccountResponse {

    UUID id;
    String name;
    @JsonProperty("accountType")
    WealthAccountTypeOptionResponse wealthAccountType;
    BigDecimal balance;
    Boolean isSynced;
    Boolean includeInNetWorth;
}
