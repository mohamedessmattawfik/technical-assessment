package com.mal.handler;

import com.mal.domain.LedgerAccount;
import com.mal.model.LedgerEntry;

public interface LedgerHandler {
    void handle(LedgerEntry ledgerEntry, LedgerAccount ledgerAccount);
}
