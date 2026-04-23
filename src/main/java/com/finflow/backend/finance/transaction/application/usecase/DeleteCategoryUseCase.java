package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.finance.transaction.application.port.in.DeleteCategoryPort;
import com.finflow.backend.finance.budget.api.BudgetReadApi;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.transaction.application.command.DeleteCategoryCommand;
import com.finflow.backend.finance.transaction.domain.entity.Category;
import com.finflow.backend.finance.transaction.domain.repository.CategoryRepository;
import com.finflow.backend.finance.transaction.domain.repository.TransactionRepository;
import com.finflow.backend.finance.transaction.exception.TransactionErrorCode;
import com.finflow.backend.finance.transaction.domain.constant.TransactionConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeleteCategoryUseCase implements DeleteCategoryPort {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetReadApi budgetReadApi;

    @Transactional
    @Override
    public void execute(DeleteCategoryCommand command) {
        String userId = command.userId();
        UUID categoryId = command.categoryId();
        log.info("Deleting category {} for userId: {}", categoryId, userId);

        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new AppException(TransactionErrorCode.CATEGORY_NOT_FOUND));

        if (TransactionConstants.SYSTEM_USER_ID.equals(category.getUserId())) {
            throw new AppException(TransactionErrorCode.CATEGORY_NOT_OWNED);
        }

        long txCount = transactionRepository.countByCategory_Id(categoryId);
        long budgetCount = budgetReadApi.countBudgetsByCategoryId(categoryId);
        if (txCount > 0 || budgetCount > 0) {
            throw new AppException(TransactionErrorCode.CATEGORY_IN_USE);
        }

        categoryRepository.delete(category);
    }
}
