package com.finflow.backend.finance.wealth.application.mapper;

import com.finflow.backend.finance.wealth.domain.entity.WealthAccountType;
import com.finflow.backend.finance.wealth.domain.entity.WealthAccount;
import com.finflow.backend.finance.wealth.presentation.response.WealthAccountTypeOptionResponse;
import com.finflow.backend.finance.wealth.presentation.response.WealthAccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WealthAccountMapper {

    @Mapping(target = "wealthAccountType", source = "wealthAccountType")
    WealthAccountResponse toWealthAccountResponse(WealthAccount wealthAccount);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "displayName", source = "displayName")
    @Mapping(target = "icon", source = "icon")
    @Mapping(target = "color", source = "color")
    @Mapping(target = "transactionEligible", source = "isTransactionEligible")
    @Mapping(target = "debt", source = "isDebt")
    WealthAccountTypeOptionResponse toWealthAccountTypeOptionResponse(WealthAccountType wealthAccountType);
}
