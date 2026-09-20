package com.mal.policy;

import com.mal.domain.ReadOnlyLedger;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface InterestPolicy {
    BigDecimal calculateDailyAccrual(ReadOnlyLedger account, LocalDate evaluationDate);
}
