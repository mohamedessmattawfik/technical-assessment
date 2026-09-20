package com.mal.reporter;

import com.mal.domain.ReadOnlyLedger;
import com.mal.model.LedgerEntry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class LedgerConsoleReporter {

    public static void printDailyReport(
            LocalDate date,
            ReadOnlyLedger ledger,
            Map<LocalDate, BigDecimal> dailyAccruals
    ) {
        BigDecimal ledgerBalance = ledger.getLedgerBalance(date);
        BigDecimal availableBalance = ledger.getAvailableBalance(date);
        BigDecimal dailyInterest = dailyAccruals.getOrDefault(date, BigDecimal.ZERO);

        System.out.printf("--- EOD Report for Date: %s ---%n", date);
        System.out.printf("Account ID:        %s%n", ledger.getAccountId());
        System.out.printf("Currency:          %s%n", ledger.getCurrency());
        System.out.printf("Ledger Balance:    %s %s%n", ledger.getCurrency(), ledgerBalance);
        System.out.printf("Available Balance: %s %s%n", ledger.getCurrency(), availableBalance);
        System.out.printf("Daily Interest:    %s %s%n", ledger.getCurrency(), dailyInterest);
        System.out.println("----------------------------------------\n");
    }

    public static void printJournal(ReadOnlyLedger ledger) {
        System.out.println("=== FULL LEDGER JOURNAL ===");
        for (LedgerEntry entry : ledger.getJournalEntries()) {
            System.out.printf("[%s] %s | Type: %-13s | Amount: %s %8.2f | ValueDate: %s%n",
                    entry.dayPosted(),
                    entry.entryId(),
                    entry.type(),
                    entry.currency(),
                    entry.amount(),
                    entry.valueDate()
            );
        }
        System.out.println("===========================\n");
    }
}