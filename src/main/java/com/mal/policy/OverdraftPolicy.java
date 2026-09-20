package com.mal.policy;

import com.mal.domain.ReadOnlyLedger;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface OverdraftPolicy {

    BigDecimal calculateFee(ReadOnlyLedger account, LocalDate evaluationDate);

}
