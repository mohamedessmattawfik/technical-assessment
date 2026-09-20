package com.mal.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record LedgerEntry(
        String entryId,
        String accountId,
        EventType type,
        BigDecimal amount,
        Currency currency,
        LocalDate dayPosted,
        LocalDate valueDate,
        String referenceId
) {

    public LedgerEntry {
        Objects.requireNonNull(entryId, "Entry ID cannot be null");
        Objects.requireNonNull(accountId, "Account ID cannot be null");
        Objects.requireNonNull(type, "Type cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        Objects.requireNonNull(dayPosted, "Posting date cannot be null");
        Objects.requireNonNull(valueDate, "Value date cannot be null");

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        amount = currency.round(amount);
    }
}
