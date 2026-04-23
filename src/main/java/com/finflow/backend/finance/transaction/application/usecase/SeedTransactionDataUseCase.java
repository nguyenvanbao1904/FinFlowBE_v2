package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.finance.transaction.domain.entity.Category;
import com.finflow.backend.finance.common.enums.CategoryType;
import com.finflow.backend.finance.transaction.domain.repository.CategoryRepository;
import com.finflow.backend.finance.transaction.application.port.in.SeedTransactionDataPort;
import com.finflow.backend.finance.transaction.domain.constant.TransactionConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeedTransactionDataUseCase implements SeedTransactionDataPort {

    private final CategoryRepository categoryRepository;

    @Transactional
    @Override
    public void execute() {
        if (categoryRepository.count() == 0) {
            log.info("No categories found. Seeding default system categories...");

            List<Category> defaultCategories = List.of(
                    // Income
                    buildCategory("Lương", CategoryType.INCOME, "banknote", "#4CAF50"),
                    buildCategory("Thưởng", CategoryType.INCOME, "gift", "#FF9800"),
                    buildCategory("Đầu tư", CategoryType.INCOME, "chart.line.uptrend.xyaxis", "#2196F3"),
                    
                    // Expense
                    buildCategory("Ăn uống", CategoryType.EXPENSE, "fork.knife", "#F44336"),
                    buildCategory("Di chuyển", CategoryType.EXPENSE, "car", "#9C27B0"),
                    buildCategory("Hóa đơn", CategoryType.EXPENSE, "doc.text", "#607D8B"),
                    buildCategory("Mua sắm", CategoryType.EXPENSE, "bag", "#E91E63"),
                    buildCategory("Sức khỏe", CategoryType.EXPENSE, "cross.case", "#00BCD4"),
                    buildCategory("Khác", CategoryType.EXPENSE, "circle.grid.2x2", "#9E9E9E")
            );

            categoryRepository.saveAll(defaultCategories);
            log.info("Successfully seeded {} default categories.", defaultCategories.size());
        } else {
            log.info("Categories already exist. Skipping seed.");
        }
    }

    private Category buildCategory(String name, CategoryType type, String icon, String color) {
        return Category.builder()
                .userId(TransactionConstants.SYSTEM_USER_ID)
                .name(name)
                .type(type)
                .icon(icon)
                .color(color)
                .build();
    }
}
