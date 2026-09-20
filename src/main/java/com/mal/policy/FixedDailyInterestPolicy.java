package com.mal.policy;

import com.mal.domain.ReadOnlyLedger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class FixedDailyInterestPolicy implements InterestPolicy {
    private final BigDecimal dailyRate;

    public FixedDailyInterestPolicy(BigDecimal dailyRate) {
        this.dailyRate = Objects.requireNonNull(dailyRate, "Daily rate cannot be null");
    }


    @Override
    public BigDecimal calculateDailyAccrual(ReadOnlyLedger account, LocalDate evaluationDate) {
        BigDecimal balance = account.getLedgerBalance(evaluationDate);
        if (balance.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal rawInterest = balance.multiply(dailyRate);
            return account.getCurrency().round(rawInterest);
        }
        return account.getCurrency().round(BigDecimal.ZERO);
    }
}
