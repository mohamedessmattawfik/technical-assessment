package com.mal;

import com.mal.domain.DefaultLedgerAccount;
import com.mal.domain.LedgerAccount;
import com.mal.engine.EventReplayEngine;
import com.mal.model.*;
import com.mal.policy.DailyOverDraftPolicy;
import com.mal.policy.FixedDailyInterestPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LedgerEngineTest {

    private static final LocalDate DAY_1 = LocalDate.of(2026, 1, 1);
    private static final LocalDate DAY_2 = LocalDate.of(2026, 1, 2);
    private static final LocalDate DAY_3 = LocalDate.of(2026, 1, 3);
    private static final LocalDate DAY_4 = LocalDate.of(2026, 1, 4);
    private static final LocalDate DAY_5 = LocalDate.of(2026, 1, 5);
    private static final LocalDate DAY_6 = LocalDate.of(2026, 1, 6);

    private LedgerAccount accountAed;
    private LedgerAccount accountBhd;
    private EventReplayEngine engine;

    @BeforeEach
    void setUp() {
        // AED Account (2-decimal precision)
        accountAed = new DefaultLedgerAccount("ACC-001", Currency.AED, BigDecimal.ZERO);

        // BHD Account (3-decimal precision)
        accountBhd = new DefaultLedgerAccount("ACC-002", Currency.BHD, BigDecimal.ZERO);

        DailyOverDraftPolicy overdraftPolicy = new DailyOverDraftPolicy(new BigDecimal("25.00"));
        FixedDailyInterestPolicy interestPolicy = new FixedDailyInterestPolicy(new BigDecimal("0.0004"));

        engine = new EventReplayEngine(overdraftPolicy, interestPolicy);
    }

    @Test
    @DisplayName("Verify Full 6-Day Replay Event Stream & State Consistency for AED Account")
    void testSixDayEventReplay() {

        // --- Day 1 ---
        // E1: CREDIT +1200.00 (posted Day 1, value Day 1)
        LedgerEntry e1 = new LedgerEntry("E1", "ACC-001", EventType.CREDIT, new BigDecimal("1200.00"), Currency.AED, DAY_1, DAY_1, null);
        engine.processLedger(e1, accountAed);
        engine.processEndOfDay(DAY_1, accountAed);

        assertEquals(new BigDecimal("1200.00"), accountAed.getLedgerBalance(DAY_1));
        assertEquals(new BigDecimal("1200.00"), accountAed.getAvailableBalance(DAY_1));

        // --- Day 2 ---
        // E2: DEBIT -950.00 (posted Day 2, value Day 2)
        LedgerEntry e2 = new LedgerEntry("E2", "ACC-001", EventType.DEBIT, new BigDecimal("950.00"), Currency.AED, DAY_2, DAY_2, null);
        engine.processLedger(e2, accountAed);
        engine.processEndOfDay(DAY_2, accountAed);

        assertEquals(new BigDecimal("250.00"), accountAed.getLedgerBalance(DAY_2));

        // --- Day 3 ---
        // E3: AUTHORIZATION -185.00 (Auth-A hold created on Day 3)
        LedgerEntry e3 = new LedgerEntry("E3", "ACC-001", EventType.AUTHORIZATION, new BigDecimal("185.00"), Currency.AED, DAY_3, DAY_3, null);
        engine.processLedger(e3, accountAed);
        engine.processEndOfDay(DAY_3, accountAed);

        // Ledger balance remains 250.00, Available balance drops to 65.00 (250 - 185)
        assertEquals(new BigDecimal("250.00"), accountAed.getLedgerBalance(DAY_3));
        assertEquals(new BigDecimal("65.00"), accountAed.getAvailableBalance(DAY_3));

        // --- Day 4 ---
        // E4: CREDIT +400.00 (posted Day 4, value Day 4)
        LedgerEntry e4 = new LedgerEntry("E4", "ACC-001", EventType.CREDIT, new BigDecimal("400.00"), Currency.AED, DAY_4, DAY_4, null);
        engine.processLedger(e4, accountAed);
        engine.processEndOfDay(DAY_4, accountAed);

        // --- Day 5 ---
        // E5: SETTLEMENT -185.00 (ref E3) -> releases hold, posts debit
        LedgerEntry e5 = new LedgerEntry("E5", "ACC-001", EventType.SETTLEMENT, new BigDecimal("185.00"), Currency.AED, DAY_5, DAY_5, "E3");
        engine.processLedger(e5, accountAed);

        // E6: DEBIT -800.00 (posted Day 5, value Day 5)
        LedgerEntry e6 = new LedgerEntry("E6", "ACC-001", EventType.DEBIT, new BigDecimal("800.00"), Currency.AED, DAY_5, DAY_5, null);
        engine.processLedger(e6, accountAed);

        // E7: BACKDATED DEBIT -620.00 (posted Day 5, value Date: Day 2!)
        LedgerEntry e7 = new LedgerEntry("E7", "ACC-001", EventType.DEBIT, new BigDecimal("620.00"), Currency.AED, DAY_5, DAY_2, null);
        engine.processLedger(e7, accountAed);

        engine.processEndOfDay(DAY_5, accountAed);

        // Verify backdated calculation: As of Day 2 value_date, balance retroactively became:
        // 1200 (E1) - 950 (E2) - 620 (E7) = -370.00
        assertEquals(new BigDecimal("-370.00"), accountAed.getLedgerBalance(DAY_2));

        // --- Day 6 ---
        // E8: REVERSAL +620.00 (reverses E7 on Day 6)
        LedgerEntry e8 = new LedgerEntry("E8", "ACC-001", EventType.REVERSAL, new BigDecimal("620.00"), Currency.AED, DAY_6, DAY_6, "E7");
        engine.processLedger(e8, accountAed);

        engine.processEndOfDay(DAY_6, accountAed);

        // Capitalize interest accrued across Days 1..6
        engine.capitalizeInterestAtWindowEnd(DAY_6, accountAed);

        // Verify journal holds settled entries + generated fee/interest entries
        assertFalse(accountAed.getJournalEntries().isEmpty());
    }
    @Test
    @DisplayName("Verify BHD Account 3-Decimal Precision, Balance Calculation, and Account Isolation")
    void testBhdAccountMultiDecimalPrecisionAndIsolation() {
        // Day 1: Credit 500.125 BHD (3 decimal places)
        LedgerEntry b1 = new LedgerEntry("B1", "ACC-002", EventType.CREDIT, new BigDecimal("500.125"), Currency.BHD, DAY_1, DAY_1, null);
        engine.processLedger(b1, accountBhd);
        engine.processEndOfDay(DAY_1, accountBhd);

        assertEquals(new BigDecimal("500.125"), accountBhd.getLedgerBalance(DAY_1));
        assertEquals(new BigDecimal("500.125"), accountBhd.getAvailableBalance(DAY_1));

        // Day 2: Authorization 150.050 BHD
        LedgerEntry b2 = new LedgerEntry("B2", "ACC-002", EventType.AUTHORIZATION, new BigDecimal("150.050"), Currency.BHD, DAY_2, DAY_2, null);
        engine.processLedger(b2, accountBhd);
        engine.processEndOfDay(DAY_2, accountBhd);

        // Ledger balance remains 500.125 BHD; Available balance becomes 350.075 BHD (500.125 - 150.050)
        assertEquals(new BigDecimal("500.125"), accountBhd.getLedgerBalance(DAY_2));
        assertEquals(new BigDecimal("350.075"), accountBhd.getAvailableBalance(DAY_2));

        // Verify Account Isolation: ACC-001 balance remains 0.00 AED (using compareTo or matching scale)
        assertEquals(0, new BigDecimal("0.00").compareTo(accountAed.getLedgerBalance(DAY_2)));
    }
}