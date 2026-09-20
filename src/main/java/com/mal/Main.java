package com.mal;

import com.mal.domain.DefaultLedgerAccount;
import com.mal.domain.LedgerAccount;
import com.mal.engine.EventReplayEngine;
import com.mal.model.*;
import com.mal.policy.DailyOverDraftPolicy;
import com.mal.policy.FixedDailyInterestPolicy;
import com.mal.reporter.LedgerConsoleReporter;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Main {

    private static final LocalDate DAY_1 = LocalDate.of(2026, 1, 1);
    private static final LocalDate DAY_2 = LocalDate.of(2026, 1, 2);
    private static final LocalDate DAY_3 = LocalDate.of(2026, 1, 3);
    private static final LocalDate DAY_4 = LocalDate.of(2026, 1, 4);
    private static final LocalDate DAY_5 = LocalDate.of(2026, 1, 5);
    private static final LocalDate DAY_6 = LocalDate.of(2026, 1, 6);

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   IN-MEMORY LEDGER CORE ENGINE SIMULATION");
        System.out.println("=================================================\n");

        // 1. Initialize Accounts
        LedgerAccount accountAed = new DefaultLedgerAccount("ACC-001", Currency.AED, BigDecimal.ZERO);
        LedgerAccount accountBhd = new DefaultLedgerAccount("ACC-002", Currency.BHD, BigDecimal.ZERO);

        // 2. Initialize Policies & Replay Engine
        DailyOverDraftPolicy overdraftPolicy = new DailyOverDraftPolicy(new BigDecimal("25.00"));
        FixedDailyInterestPolicy interestPolicy = new FixedDailyInterestPolicy(new BigDecimal("0.0004"));
        EventReplayEngine engine = new EventReplayEngine(overdraftPolicy, interestPolicy);

        // -----------------------------------------------------------------
        // EXECUTE 6-DAY EVENT STREAM FOR ACC-001 (AED)
        // -----------------------------------------------------------------

        // --- Day 1 ---
        LedgerEntry e1 = new LedgerEntry("E1", "ACC-001", EventType.CREDIT, new BigDecimal("1200.00"), Currency.AED, DAY_1, DAY_1, null);
        engine.processLedger(e1, accountAed);
        engine.processEndOfDay(DAY_1, accountAed);
        LedgerConsoleReporter.printDailyReport(DAY_1, accountAed, engine.getDailyAccruals());

        // --- Day 2 ---
        LedgerEntry e2 = new LedgerEntry("E2", "ACC-001", EventType.DEBIT, new BigDecimal("950.00"), Currency.AED, DAY_2, DAY_2, null);
        engine.processLedger(e2, accountAed);
        engine.processEndOfDay(DAY_2, accountAed);
        LedgerConsoleReporter.printDailyReport(DAY_2, accountAed, engine.getDailyAccruals());

        // --- Day 3 ---
        LedgerEntry e3 = new LedgerEntry("E3", "ACC-001", EventType.AUTHORIZATION, new BigDecimal("185.00"), Currency.AED, DAY_3, DAY_3, null);
        engine.processLedger(e3, accountAed);
        engine.processEndOfDay(DAY_3, accountAed);
        LedgerConsoleReporter.printDailyReport(DAY_3, accountAed, engine.getDailyAccruals());

        // --- Day 4 ---
        LedgerEntry e4 = new LedgerEntry("E4", "ACC-001", EventType.CREDIT, new BigDecimal("400.00"), Currency.AED, DAY_4, DAY_4, null);
        engine.processLedger(e4, accountAed);
        engine.processEndOfDay(DAY_4, accountAed);
        LedgerConsoleReporter.printDailyReport(DAY_4, accountAed, engine.getDailyAccruals());

        // --- Day 5 ---
        LedgerEntry e5 = new LedgerEntry("E5", "ACC-001", EventType.SETTLEMENT, new BigDecimal("185.00"), Currency.AED, DAY_5, DAY_5, "E3");
        engine.processLedger(e5, accountAed);

        LedgerEntry e6 = new LedgerEntry("E6", "ACC-001", EventType.DEBIT, new BigDecimal("800.00"), Currency.AED, DAY_5, DAY_5, null);
        engine.processLedger(e6, accountAed);

        LedgerEntry e7 = new LedgerEntry("E7", "ACC-001", EventType.DEBIT, new BigDecimal("620.00"), Currency.AED, DAY_5, DAY_2, null);
        engine.processLedger(e7, accountAed);

        engine.processEndOfDay(DAY_5, accountAed);
        LedgerConsoleReporter.printDailyReport(DAY_5, accountAed, engine.getDailyAccruals());

        // --- Day 6 ---
        LedgerEntry e8 = new LedgerEntry("E8", "ACC-001", EventType.REVERSAL, new BigDecimal("620.00"), Currency.AED, DAY_6, DAY_6, "E7");
        engine.processLedger(e8, accountAed);
        engine.processEndOfDay(DAY_6, accountAed);

        // Capitalize Interest at Window Close
        engine.capitalizeInterestAtWindowEnd(DAY_6, accountAed);
        LedgerConsoleReporter.printDailyReport(DAY_6, accountAed, engine.getDailyAccruals());

        // Print Full Journal for ACC-001
        LedgerConsoleReporter.printJournal(accountAed);

        // -----------------------------------------------------------------
        // EXECUTE BHD MULTI-DECIMAL & ISOLATION SIMULATION FOR ACC-002 (BHD)
        // -----------------------------------------------------------------
        System.out.println("Executing BHD Account (3-Decimal Precision) Verification...\n");

        LedgerEntry b1 = new LedgerEntry("B1", "ACC-002", EventType.CREDIT, new BigDecimal("500.125"), Currency.BHD, DAY_1, DAY_1, null);
        engine.processLedger(b1, accountBhd);
        engine.processEndOfDay(DAY_1, accountBhd);

        LedgerEntry b2 = new LedgerEntry("B2", "ACC-002", EventType.AUTHORIZATION, new BigDecimal("150.050"), Currency.BHD, DAY_2, DAY_2, null);
        engine.processLedger(b2, accountBhd);
        engine.processEndOfDay(DAY_2, accountBhd);

        LedgerConsoleReporter.printDailyReport(DAY_2, accountBhd, engine.getDailyAccruals());
        LedgerConsoleReporter.printJournal(accountBhd);

        System.out.println("Simulation complete. All ledger entries and reports generated successfully.");
    }
}