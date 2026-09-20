package com.mal.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record Hold(
        String authId,
        String accountId,
        BigDecimal amount,
        LocalDate dateCreated
) {
    public Hold {
        Objects.requireNonNull(authId, "Auth ID cannot be null");
        Objects.requireNonNull(accountId, "Account ID cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(dateCreated, "Creation date cannot be null");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Hold amount must be positive");
        }
    }
}