package com.finflow.backend.investment.portfolio.presentation.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatePortfolioRequest {

    @NotBlank(message = "PORTFOLIO_NAME_BLANK")
    String name;

    UUID wealthAccountId;
}
