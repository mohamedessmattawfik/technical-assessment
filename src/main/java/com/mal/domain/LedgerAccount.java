package com.mal.domain;

import com.mal.model.Hold;
import com.mal.model.LedgerEntry;

import java.util.Optional;

public interface LedgerAccount extends ReadOnlyLedger {
    void appendEntry(LedgerEntry entry);
    void addHold(Hold hold);
    void removeHold(String authId);
    Optional<Hold> getHold(String authId);
}
