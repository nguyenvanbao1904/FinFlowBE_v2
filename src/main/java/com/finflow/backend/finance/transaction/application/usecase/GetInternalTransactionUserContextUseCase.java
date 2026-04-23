package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.finance.transaction.application.dto.InternalTransactionUserContextOutput;
import com.finflow.backend.finance.transaction.application.port.in.GetInternalTransactionUserContextPort;
import com.finflow.backend.finance.transaction.application.query.GetInternalTransactionUserContextQuery;
import com.finflow.backend.finance.wealth.api.WealthAccountApi;
import com.finflow.backend.finance.transaction.domain.entity.Category;
import com.finflow.backend.finance.transaction.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetInternalTransactionUserContextUseCase implements GetInternalTransactionUserContextPort {

    private final CategoryRepository categoryRepository;
    private final WealthAccountApi wealthAccountApi;

    @Override
    @Transactional(readOnly = true)
    public InternalTransactionUserContextOutput execute(GetInternalTransactionUserContextQuery query) {
        String userId = query.userId();
        List<Category> categories = categoryRepository.findByUserIdOrSystem(userId);
        List<InternalTransactionUserContextOutput.ContextCategory> categoryList = categories.stream()
                .map(c -> new InternalTransactionUserContextOutput.ContextCategory(
                        c.getId().toString(),
                        c.getName(),
                        c.getType().name(),
                        c.getIcon()))
                .collect(Collectors.toList());

        List<WealthAccountApi.AccountSnapshot> accounts = wealthAccountApi.findAllAccountsWithType(userId);
        List<InternalTransactionUserContextOutput.ContextAccount> accountList = accounts.stream()
                .filter(WealthAccountApi.AccountSnapshot::transactionEligible)
                .map(a -> new InternalTransactionUserContextOutput.ContextAccount(
                        a.id().toString(),
                        a.name(),
                        a.typeDisplayName(),
                        a.balance()))
                .collect(Collectors.toList());

        return new InternalTransactionUserContextOutput(categoryList, accountList);
    }
}
