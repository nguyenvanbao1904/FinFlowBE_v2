package com.finflow.backend.finance.wealth.application.mapper;

import com.finflow.backend.finance.wealth.application.dto.WealthAccountOutput;
import com.finflow.backend.finance.wealth.application.dto.WealthAccountTypeOptionOutput;
import com.finflow.backend.finance.wealth.domain.entity.WealthAccountType;
import com.finflow.backend.finance.wealth.domain.entity.WealthAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WealthAccountMapper {

    @Mapping(target = "wealthAccountType", source = "wealthAccountType")
    WealthAccountOutput toWealthAccountOutput(WealthAccount wealthAccount);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "displayName", source = "displayName")
    @Mapping(target = "icon", source = "icon")
    @Mapping(target = "color", source = "color")
    @Mapping(target = "transactionEligible", source = "isTransactionEligible")
    @Mapping(target = "debt", source = "isDebt")
    WealthAccountTypeOptionOutput toWealthAccountTypeOptionOutput(WealthAccountType wealthAccountType);
}
