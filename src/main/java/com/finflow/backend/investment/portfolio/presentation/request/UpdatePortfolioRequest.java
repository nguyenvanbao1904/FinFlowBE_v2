package com.finflow.backend.investment.portfolio.presentation.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdatePortfolioRequest {

    @NotBlank(message = "PORTFOLIO_NAME_BLANK")
    String name;
}
