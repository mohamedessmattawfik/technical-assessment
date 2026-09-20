package com.mal.handler;

import com.mal.domain.LedgerAccount;
import com.mal.model.LedgerEntry;

public class DebitLedgerHandler implements LedgerHandler {

    @Override
    public void handle(LedgerEntry ledgerEntry, LedgerAccount ledgerAccount) {
        ledgerAccount.appendEntry(ledgerEntry);
    }
}
