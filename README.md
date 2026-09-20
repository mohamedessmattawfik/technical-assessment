# In-Memory Ledger Core Service

A production-grade, append-only, event-sourced in-memory financial ledger implemented in Java. Designed for precision financial modeling, dual-balance tracking (Ledger vs. Available), backdated value-date evaluation, and automated end-of-day (EOD) policy enforcement over a 6-day evaluation window.

---

## Architectural Highlights

* **CQRS Read-Write Segregation**: The domain layer splits state mutation (`LedgerAccount`) from inspection views (`ReadOnlyLedger`), ensuring that policies (like overdraft calculation and interest accrual) are pure, side-effect-free functions.
* **Temporal Modeling (`LocalDate`)**: Uses `java.time.LocalDate` to handle chronological posting dates and retroactive value-date calculations (`value_date <= targetDate`).
* **Strategy Pattern (Policies)**: Business rules are isolated into pluggable policies (`DailyOverDraftPolicy`, `FixedDailyInterestPolicy`) adhering to the Open/Closed Principle (OCP).
* **Command Handler Pattern**: Events are routed through dedicated polymorphic handlers (`CreditLedgerHandler`, `DebitLedgerHandler`, `AuthorizationLedgerHandler`, `SettlementLedgerHandler`, `ReversalLedgerHandler`) to manage journal appends and hold lifecycles.
* **Deterministic Precision**: Financial amounts are managed via `java.math.BigDecimal` with explicit currency scaling (`AED` scale 2, `BHD` scale 3) and `RoundingMode.HALF_UP`.

---

## Project Structure

```text
src/
├── main/
│   └── java/
│       └── com/
│           └── mal/
│               ├── domain/       # ReadOnlyLedger, LedgerAccount, DefaultLedgerAccount
│               ├── engine/       # EventReplayEngine (replays events and runs EOD batches)
│               ├── handler/      # Polymorphic event handlers
│               ├── model/        # Currency, EventType, LedgerEntry, Hold records
│               └── policy/       # OverdraftPolicy, InterestPolicy strategy implementations
└── test/
    └── java/
        └── com/
            └── mal/
                ├── LedgerEngineTest.java            # Full 6-day E2E integration test
                └── IntentionalDesignFailureTest.java # Boundary & guardrail assertions
```
## Getting Started & Running Tests

### Prerequisites
* **Java Development Kit (JDK) 24+** (configured in `pom.xml`)
* **Maven 3.8+**

### Build and Test Execution

Run the test suite using Maven:

```bash
mvn clean test
```
The test runner executes the 6-day evaluation window simulation (E1 through E8), verifying daily closing ledger balances, available spending balances, backdated value-date audits, and end-of-window interest capitalization.

---

## Repository Documentation Deliverables

Detailed engineering and audit records are available in the repository root:

* [`NUMBERS.md`](NUMBERS.md) — Day-by-day mathematical audit breakdown and final interest capitalization calculations.
* [`AMBIGUITIES.md`](AMBIGUITIES.md) — Documentation of functional ambiguities encountered and their engineering resolutions.
* [`REJECTED.md`](REJECTED.md) — Architectural patterns evaluated and explicitly rejected in favor of robust ledger design.
* [`WORKLOG.md`](WORKLOG.md) — Chronological execution log documenting the development phases from domain modeling to test validation.
