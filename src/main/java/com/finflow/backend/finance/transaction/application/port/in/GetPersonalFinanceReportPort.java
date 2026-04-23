package com.finflow.backend.finance.transaction.application.port.in;

import com.finflow.backend.finance.transaction.application.dto.PersonalFinanceReportOutput;
import com.finflow.backend.finance.transaction.application.query.GetPersonalFinanceReportQuery;

public interface GetPersonalFinanceReportPort {

    PersonalFinanceReportOutput execute(GetPersonalFinanceReportQuery query);
}
