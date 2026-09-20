package com.mal.handler;

import com.mal.domain.LedgerAccount;
import com.mal.model.Hold;
import com.mal.model.LedgerEntry;

public class AuthorizationLedgerHandler implements LedgerHandler {

    @Override
    public void handle(LedgerEntry ledgerEntry, LedgerAccount ledgerAccount) {
        Hold hold = new Hold(ledgerEntry.entryId(), ledgerEntry.accountId(), ledgerEntry.amount(), ledgerEntry.dayPosted());
        ledgerAccount.addHold(hold);
    }
}
