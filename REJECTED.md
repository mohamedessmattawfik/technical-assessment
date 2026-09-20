# REJECTED.md — Evaluated and Rejected Architectural Alternatives

To maintain strict financial integrity, high concurrency performance, and clean maintainability, several design alternatives were evaluated and explicitly rejected.

---

### 1. In-Place Mutable Account Balance Fields
* **Rejected Design**: Maintaining `private BigDecimal ledgerBalance` and `private BigDecimal availableBalance` fields directly inside `DefaultLedgerAccount` and mutating them incrementally upon every event.
* **Reason for Rejection**:
    * Mutating balance fields directly breaks event sourcing and auditability.
    * In-place balances cannot compute backdated balance states (`value_date <= targetDate`) without complex, error-prone rollback mechanics.
    * **Chosen Alternative**: Compute balances dynamically by querying the immutable append-only `journal` stream using `getLedgerBalance(LocalDate)`.

---

### 2. Primitive `double` or `float` for Financial Amounts
* **Rejected Design**: Using IEEE 754 floating-point numbers (`double` or `float`) for financial calculations.
* **Reason for Rejection**:
    * Binary floating-point representation introduces representation errors (e.g., `0.1 + 0.2 = 0.30000000000000004`), which lead to currency drift in financial systems.
    * **Chosen Alternative**: Strict use of `java.math.BigDecimal` with explicit scale definitions (`AED = 2`, `BHD = 3`) and `RoundingMode.HALF_UP`.

---

### 3. Raw `int` or Relative Integer Counters for Business Days
* **Rejected Design**: Representing processing days as raw integers (`int day = 1`, `int day = 2`).
* **Reason for Rejection**:
    * Integer days lack calendar semantics, ISO date parsing capabilities, leap year handling, and realistic temporal querying.
    * **Chosen Alternative**: Standardized use of `java.time.LocalDate` mapped to an anchor timeline (`2026-01-01` to `2026-01-06`).

---

### 4. Direct Coupling of Policy Logic into the Ledger Account
* **Rejected Design**: Embedding overdraft fee rules and interest rate logic inside `DefaultLedgerAccount` methods.
* **Reason for Rejection**:
    * Violates the **Single Responsibility Principle (SRP)** and **Open/Closed Principle (OCP)**. Changing fee or interest logic would require modifying core domain account code.
    * **Chosen Alternative**: Strategy Pattern via `OverdraftPolicy` and `InterestPolicy` interfaces interacting with accounts exclusively through `ReadOnlyLedger` views.