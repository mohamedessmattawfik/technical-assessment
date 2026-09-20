package com.mal.domain;

import com.mal.model.Currency;
import com.mal.model.Hold;
import com.mal.model.LedgerEntry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


public class DefaultLedgerAccount implements LedgerAccount {

    private final String accountId;
    private final Currency currency;
    private final BigDecimal openingBalance;

    private final List<LedgerEntry> journal = new ArrayList<>();
    private final Map<String, Hold> activeHolds = new LinkedHashMap<>();

    public DefaultLedgerAccount(String accountId, Currency currency, BigDecimal openingBalance) {
        this.accountId = Objects.requireNonNull(accountId, "Account ID cannot be null");
        this.currency = Objects.requireNonNull(currency, "Currency cannot be null");
        BigDecimal base = Objects.requireNonNullElse(openingBalance, BigDecimal.ZERO);
        this.openingBalance = currency.round(base);
    }

    @Override public String getAccountId() { return accountId; }
    @Override public Currency getCurrency() { return currency; }

    @Override
    public synchronized void appendEntry(LedgerEntry entry) {
        Objects.requireNonNull(entry, "Ledger entry cannot be null");
        if (!entry.accountId().equals(this.accountId)) {
            throw new IllegalArgumentException("Account mismatch for entry: " + entry.accountId());
        }
        journal.add(entry);
    }

    @Override
    public synchronized void addHold(Hold hold) {
        Objects.requireNonNull(hold, "Hold cannot be null");
        if (!hold.accountId().equals(this.accountId)) {
            throw new IllegalArgumentException("Account mismatch for hold: " + hold.accountId());
        }
        activeHolds.put(hold.authId(), hold);
    }

    @Override
    public synchronized void removeHold(String authId) {
        Objects.requireNonNull(authId, "Auth ID cannot be null");
        activeHolds.remove(authId);
    }

    @Override
    public synchronized Optional<Hold> getHold(String authId) {
        return Optional.ofNullable(activeHolds.get(authId));
    }

    @Override
    public synchronized BigDecimal getLedgerBalance(LocalDate asOfValueDate) {
        BigDecimal balance = openingBalance;
        for (LedgerEntry entry : journal) {
            if (!entry.valueDate().isAfter(asOfValueDate)) {
                switch (entry.type()) {
                    case CREDIT, INTEREST_CREDIT, REVERSAL -> balance = balance.add(entry.amount());
                    case DEBIT, SETTLEMENT, OVERDRAFT_FEE -> balance = balance.subtract(entry.amount());
                    default -> {}
                }
            }
        }
        return currency.round(balance);
    }

    @Override
    public synchronized BigDecimal getAvailableBalance(LocalDate asOfValueDate) {
        BigDecimal ledgerBal = getLedgerBalance(asOfValueDate);
        BigDecimal totalHolds = activeHolds.values().stream()
                .map(Hold::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return currency.round(ledgerBal.subtract(totalHolds));
    }

    @Override
    public synchronized List<LedgerEntry> getJournalEntries() {
        return Collections.unmodifiableList(new ArrayList<>(journal));
    }
}