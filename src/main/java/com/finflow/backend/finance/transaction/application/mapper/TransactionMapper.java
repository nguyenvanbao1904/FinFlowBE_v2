package com.finflow.backend.finance.transaction.application.mapper;

import com.finflow.backend.finance.transaction.application.dto.TransactionOutput;
import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface TransactionMapper {

    @Mapping(source = "wealthAccountId", target = "accountId")
    TransactionOutput toTransactionOutput(Transaction transaction);
}
