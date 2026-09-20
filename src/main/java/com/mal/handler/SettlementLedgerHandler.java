package com.mal.handler;

import com.mal.domain.LedgerAccount;
import com.mal.model.LedgerEntry;

public class SettlementLedgerHandler implements LedgerHandler {

    @Override
    public void handle(LedgerEntry ledgerEntry, LedgerAccount account) {
        if (ledgerEntry.referenceId() != null && !ledgerEntry.referenceId().isBlank()) {
            account.getHold(ledgerEntry.referenceId()).ifPresent(hold ->
                    account.removeHold(ledgerEntry.referenceId())
            );
        }
        account.appendEntry(ledgerEntry);
    }
}
