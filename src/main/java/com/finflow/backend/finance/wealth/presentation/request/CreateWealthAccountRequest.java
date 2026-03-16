package com.finflow.backend.finance.wealth.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateWealthAccountRequest {

    @NotBlank(message = "WEALTH_ACCOUNT_NAME_BLANK")
    String name;

    @NotNull(message = "WEALTH_ACCOUNT_TYPE_REQUIRED")
    UUID accountTypeId;

    @NotNull(message = "WEALTH_ACCOUNT_BALANCE_REQUIRED")
    BigDecimal balance;

    // Optional: default handled in use case (true when null)
    Boolean includeInNetWorth;
}
