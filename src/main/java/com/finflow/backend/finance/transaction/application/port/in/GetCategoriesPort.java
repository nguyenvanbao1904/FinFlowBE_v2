package com.finflow.backend.finance.transaction.application.port.in;
import com.finflow.backend.finance.transaction.application.query.GetCategoriesQuery;

import com.finflow.backend.finance.transaction.application.dto.CategoryOutput;
import java.util.List;

public interface GetCategoriesPort {
    List<CategoryOutput> execute(GetCategoriesQuery query);
}
