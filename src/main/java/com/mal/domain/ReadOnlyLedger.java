package com.mal.domain;

import com.mal.model.Currency;
import com.mal.model.LedgerEntry;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ReadOnlyLedger {
    String getAccountId();
    Currency getCurrency();
    BigDecimal getLedgerBalance(LocalDate asOfValueDate);
    BigDecimal getAvailableBalance(LocalDate asOfValueDate);
    List<LedgerEntry> getJournalEntries();
}
