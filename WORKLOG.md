# WORKLOG.md — Chronological Engineering Execution Log

---

### Phase 1: Domain & Model Definition
* Defined core domain models as immutable Java records: `Currency`, `EventType`, `LedgerEntry`, and `Hold`.
* Implemented currency scale enforcement (`AED` scale 2, `BHD` scale 3) with `RoundingMode.HALF_UP` inside record canonical constructors.
* Enforced invariant guards: rejected negative transaction amounts and null identifiers.

---

### Phase 2: Domain Abstraction & CQRS Read View
* Created `ReadOnlyLedger` interface to expose safe query views (`getLedgerBalance`, `getAvailableBalance`, `getJournalEntries`).
* Created `LedgerAccount` interface extending `ReadOnlyLedger` with mutation capabilities (`appendEntry`, `addHold`, `removeHold`).
* Implemented `DefaultLedgerAccount` with synchronized, thread-safe collections (`List<LedgerEntry>` and `Map<String, Hold>`).

---

### Phase 3: Policy Strategy Layer Implementation
* Implemented strategy interfaces: `OverdraftPolicy` and `InterestPolicy`.
* Implemented `DailyOverdraftPolicy`: assesses fixed fee (e.g., 25.00 AED) if closing ledger balance is negative (`< 0`).
* Implemented `FixedDailyInterestPolicy`: calculates daily accrual ($0.04\%$) on positive closing ledger balance (`> 0`) with immediate daily rounding per currency scale.

---

### Phase 4: Command Handler Pattern Setup
* Implemented polymorphic event handlers for journal postings:
    * `CreditLedgerHandler` & `DebitLedgerHandler`: append settled ledger entries.
    * `AuthorizationLedgerHandler`: creates active hold against available balance.
    * `SettlementLedgerHandler`: releases hold by `referenceId` and appends settled debit entry.
    * `ReversalLedgerHandler`: appends counter-balancing entry.

---

### Phase 5: Orchestration Engine & EOD Batch Processing
* Created `EventReplayEngine` to route entries to appropriate handlers and execute daily EOD routines.
* Integrated `TreeMap<LocalDate, BigDecimal>` for chronological, deterministic tracking of daily interest accruals.
* Implemented `capitalizeInterestAtWindowEnd(...)` to sum pre-rounded daily accruals and post a single `INTEREST_CREDIT` entry at window close (Day 6).

---

### Phase 6: Build Configuration & Unit Test Suite
* Configured `pom.xml` with Java compiler target set to `24`, adding `org.junit.jupiter:junit-jupiter:5.10.2` and `maven-surefire-plugin`.
* Built `LedgerEngineTest.java` executing the complete 6-day replay scenario (`E1` through `E8`), verifying daily balances, backdated value date behavior on Day 2, and interest capitalization.
* Built `IntentionalDesignFailureTest.java` to explicitly test domain boundary constraints (negative amounts, account isolation mismatch, and orphan settlements).