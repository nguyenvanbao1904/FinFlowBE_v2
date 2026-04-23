package com.finflow.backend.finance.wealth.presentation.mapper;

import com.finflow.backend.finance.wealth.application.dto.WealthAccountOutput;
import com.finflow.backend.finance.wealth.application.dto.WealthAccountTypeOptionOutput;
import com.finflow.backend.finance.wealth.presentation.response.WealthAccountResponse;
import com.finflow.backend.finance.wealth.presentation.response.WealthAccountTypeOptionResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE,
        unmappedSourcePolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface WealthPresentationMapper {

    WealthAccountTypeOptionResponse toResponse(WealthAccountTypeOptionOutput output);

    WealthAccountResponse toResponse(WealthAccountOutput output);

    List<WealthAccountTypeOptionResponse> toTypeResponses(List<WealthAccountTypeOptionOutput> outputs);

    List<WealthAccountResponse> toAccountResponses(List<WealthAccountOutput> outputs);
}
