package com.finflow.backend.finance.transaction.application.port.in;

import java.util.Map;

public interface GetInternalTransactionUserContextPort {

    Map<String, Object> execute(String userId);
}
