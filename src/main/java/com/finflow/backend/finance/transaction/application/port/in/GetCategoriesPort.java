package com.finflow.backend.finance.transaction.application.port.in;

import com.finflow.backend.finance.transaction.presentation.response.CategoryResponse;
import java.util.List;

public interface GetCategoriesPort {
    List<CategoryResponse> execute(String userId);
}
