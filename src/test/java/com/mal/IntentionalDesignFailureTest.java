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

/**
 * Technical Assessment Design Boundary & Intentional Failure Suite.
 *
 * This suite serves two purposes:
 * 1. Proves that domain invariants and boundary conditions are strictly guarded (negative amounts, account isolation).
 * 2. Provides intentional boundary failure demonstrations to highlight system limits under invalid state transitions.
 */
class IntentionalDesignFailureTest {

    private static final LocalDate DAY_1 = LocalDate.of(2026, 1, 1);
    private LedgerAccount account;
    private EventReplayEngine engine;

    @BeforeEach
    void setUp() {
        account = new DefaultLedgerAccount("ACC-001", Currency.AED, BigDecimal.ZERO);
        DailyOverDraftPolicy overdraftPolicy = new DailyOverDraftPolicy(new BigDecimal("25.00"));
        FixedDailyInterestPolicy interestPolicy = new FixedDailyInterestPolicy(new BigDecimal("0.0004"));
        engine = new EventReplayEngine(overdraftPolicy, interestPolicy);
    }

    @Test
    @DisplayName("Design Boundary: Reject negative transaction amounts at model construction")
    void testRejectNegativeLedgerEntryAmount() {
        /*
         * DESIGN INVARIANT:
         * Financial ledgers must never accept negative amounts in transaction records.
         * The directional impact (Debit vs Credit) is governed strictly by EventType.
         */
        assertThrows(IllegalArgumentException.class, () -> {
            new LedgerEntry(
                    "ERR-1",
                    "ACC-001",
                    EventType.DEBIT,
                    new BigDecimal("-50.00"), // Negative amount forbidden
                    Currency.AED,
                    DAY_1,
                    DAY_1,
                    null
            );
        }, "LedgerEntry constructor must throw IllegalArgumentException when amount is negative");
    }

    @Test
    @DisplayName("Design Boundary: Reject posting entry to mismatched target account")
    void testRejectAccountMismatch() {
        LedgerEntry entryForAccountB = new LedgerEntry(
                "E1",
                "ACC-999", // Target account ID mismatch
                EventType.CREDIT,
                new BigDecimal("100.00"),
                Currency.AED,
                DAY_1,
                DAY_1,
                null
        );

        /*
         * DESIGN INVARIANT:
         * DefaultLedgerAccount must enforce strict account isolation.
         * Attempting to append an entry targeted for ACC-999 into ACC-001's journal must fail.
         */
        assertThrows(IllegalArgumentException.class, () -> {
            account.appendEntry(entryForAccountB);
        }, "DefaultLedgerAccount must reject entries intended for a different account ID");
    }

    @Test
    @DisplayName("Intentional Design Failure: Attempting to settle a non-existent authorization hold")
    void testIntentionalDesignFailure_OrphanSettlementHoldRelease() {
        /*
         * INTENTIONAL FAILURE DEMONSTRATION:
         * This test demonstrates the boundary behavior when a SETTLEMENT entry arrives with a
         * reference ID pointing to an Authorization hold ('AUTH-NON-EXISTENT') that was never registered.
         *
         * Expected Behavior:
         * The engine handles missing holds safely without throwing unhandled exceptions,
         * but the hold removal operation returns false/empty, showing that no state mutated in active holds.
         */
        LedgerEntry orphanSettlement = new LedgerEntry(
                "E_ORPHAN",
                "ACC-001",
                EventType.SETTLEMENT,
                new BigDecimal("100.00"),
                Currency.AED,
                DAY_1,
                DAY_1,
                "AUTH-NON-EXISTENT" // Non-existent hold reference
        );

        // Process orphan settlement
        assertDoesNotThrow(() -> engine.processLedger(orphanSettlement, account));

        // Verify ledger journal recorded the entry but hold registry remains empty
        assertEquals(1, account.getJournalEntries().size());
        assertTrue(account.getHold("AUTH-NON-EXISTENT").isEmpty(), "No hold should exist for unreferenced auth ID");

        /*
         * INTENTIONAL ASSERTION FAIL DEMONSTRATION (Commented out for build success):
         * Direct assertion showing what fails if an integration test strictly expects an existing hold:
         *
         * assertTrue(account.getHold("AUTH-NON-EXISTENT").isPresent(),
         *     "INTENTIONAL FAIL: Attempted to fetch hold that was never authorized.");
         */
    }
}