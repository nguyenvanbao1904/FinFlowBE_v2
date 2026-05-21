package com.finflow.backend.finance.wealth.application.mapper;

import com.finflow.backend.finance.wealth.application.dto.WealthAccountOutput;
import com.finflow.backend.finance.wealth.application.dto.WealthAccountTypeOptionOutput;
import com.finflow.backend.finance.wealth.domain.entity.WealthAccountType;
import com.finflow.backend.finance.wealth.domain.entity.WealthAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring")
public abstract class WealthAccountMapper {

    private static final Set<String> INVESTMENT_CODES = Set.of("BROKERAGE", "STOCK", "CRYPTO");

    @Mapping(target = "wealthAccountType", source = "wealthAccountType")
    public abstract WealthAccountOutput toWealthAccountOutput(WealthAccount wealthAccount);

    @Mapping(target = "transactionEligible", source = "isTransactionEligible")
    @Mapping(target = "debt", source = "isDebt")
    @Mapping(target = "group", expression = "java(resolveGroup(wealthAccountType))")
    public abstract WealthAccountTypeOptionOutput toWealthAccountTypeOptionOutput(WealthAccountType wealthAccountType);

    protected String resolveGroup(WealthAccountType type) {
        if (Boolean.TRUE.equals(type.getIsDebt())) return "DEBT";
        if (INVESTMENT_CODES.contains(type.getCode())) return "INVESTMENT";
        if (Boolean.TRUE.equals(type.getIsTransactionEligible())) return "LIQUID";
        return "ASSET";
    }
}
