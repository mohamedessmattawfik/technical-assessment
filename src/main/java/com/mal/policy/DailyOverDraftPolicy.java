package com.mal.policy;

import com.mal.domain.ReadOnlyLedger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class DailyOverDraftPolicy implements OverdraftPolicy {

    private final BigDecimal feeAmount;

    public DailyOverDraftPolicy(BigDecimal feeAmount) {
        this.feeAmount = Objects.requireNonNull(feeAmount, "Fee amount cannot be null");
    }

    @Override
    public BigDecimal calculateFee(ReadOnlyLedger account, LocalDate evaluationDate) {
        BigDecimal balance = account.getLedgerBalance(evaluationDate);
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            return account.getCurrency().round(feeAmount);
        }
        return account.getCurrency().round(BigDecimal.ZERO);
    }

}
