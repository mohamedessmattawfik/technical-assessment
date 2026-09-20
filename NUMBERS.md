# NUMBERS.md — Mathematical Audit & Balance Breakdown

This document provides a day-by-day mathematical audit of the 6-day evaluation window (mapped to `2026-01-01` through `2026-01-06`) for account `ACC-001` (Currency: AED, Precision: 2 decimals, `RoundingMode.HALF_UP`).

---

## Daily Mathematical Audit

### Day 1 (`2026-01-01`)
* **Events Processed**:
    * `E1`: CREDIT +1,200.00 AED (Value Date: `2026-01-01`)
* **Calculations**:
    * Opening Balance: 0.00 AED
    * Net Transactions: +1,200.00 AED
    * Closing Ledger Balance: **1,200.00 AED**
    * Active Holds: 0.00 AED
    * Available Balance: **1,200.00 AED**
* **End-of-Day EOD Assessment**:
    * Overdraft Fee Assessment: Balance >= 0.00 → **0.00 AED**
    * Daily Interest Accrual: $1,200.00 \times 0.0004 = 0.48$ AED → **0.48 AED**

---

### Day 2 (`2026-01-02`)
* **Events Processed**:
    * `E2`: DEBIT -950.00 AED (Value Date: `2026-01-02`)
* **Calculations**:
    * As-of `2026-01-02` Ledger Balance: $1,200.00 - 950.00 =$ **250.00 AED**
    * Active Holds: 0.00 AED
    * Available Balance: **250.00 AED**
* **End-of-Day EOD Assessment**:
    * Overdraft Fee Assessment: Balance >= 0.00 → **0.00 AED**
    * Daily Interest Accrual: $250.00 \times 0.0004 = 0.10$ AED → **0.10 AED**

---

### Day 3 (`2026-01-03`)
* **Events Processed**:
    * `E3`: AUTHORIZATION 185.00 AED (Hold Created: `E3`, Amount: 185.00 AED)
* **Calculations**:
    * Ledger Balance (Value Date $\le$ `2026-01-03`): **250.00 AED** (Authorizations do not mutate Ledger Balance)
    * Active Holds: `E3` = 185.00 AED
    * Available Balance: $250.00 - 185.00 =$ **65.00 AED**
* **End-of-Day EOD Assessment**:
    * Overdraft Fee Assessment: Ledger Balance >= 0.00 → **0.00 AED**
    * Daily Interest Accrual: $250.00 \times 0.0004 = 0.10$ AED → **0.10 AED**

---

### Day 4 (`2026-01-04`)
* **Events Processed**:
    * `E4`: CREDIT +400.00 AED (Value Date: `2026-01-04`)
* **Calculations**:
    * Ledger Balance: $250.00 + 400.00 =$ **650.00 AED**
    * Active Holds: `E3` = 185.00 AED
    * Available Balance: $650.00 - 185.00 =$ **465.00 AED**
* **End-of-Day EOD Assessment**:
    * Overdraft Fee Assessment: Balance >= 0.00 → **0.00 AED**
    * Daily Interest Accrual: $650.00 \times 0.0004 = 0.26$ AED → **0.26 AED**

---

### Day 5 (`2026-01-05`) — Backdated Event Processing Day
* **Events Processed**:
    * `E5`: SETTLEMENT 185.00 AED (Value Date: `2026-01-05`, Ref: `E3`) → Hold `E3` released; Debit 185.00 AED posted.
    * `E6`: DEBIT -800.00 AED (Value Date: `2026-01-05`)
    * `E7`: DEBIT -620.00 AED (**Backdated Value Date: `2026-01-02`**)
* **Retroactive Balance Audits**:
    * **Retroactive Day 2 Value-Date Balance**: $1,200.00 (\text{E1}) - 950.00 (\text{E2}) - 620.00 (\text{E7}) =$ **-370.00 AED**
* **Day 5 Closing Calculations**:
    * Net Journal Position as of `2026-01-05`:
      $+1,200.00 (\text{E1}) - 950.00 (\text{E2}) + 400.00 (\text{E4}) - 185.00 (\text{E5}) - 800.00 (\text{E6}) - 620.00 (\text{E7}) =$ **-955.00 AED**
    * Active Holds: 0.00 AED (Hold `E3` successfully released by `E5`)
    * Available Balance: **-955.00 AED**
* **End-of-Day EOD Assessment**:
    * Overdraft Fee Assessment: Closing Ledger Balance (-955.00 AED) < 0.00 → **OVERDRAFT_FEE of 25.00 AED assessed** (`FEE-2026-01-05`)
    * Post-Fee Closing Balance: **-980.00 AED**
    * Daily Interest Accrual: Balance < 0.00 → **0.00 AED**

---

### Day 6 (`2026-01-06`) — Window End & Capitalization
* **Events Processed**:
    * `E8`: REVERSAL +620.00 AED (Value Date: `2026-01-06`, Ref: `E7`) → Reverses backdated debit `E7`.
* **Calculations**:
    * Ledger Balance as of `2026-01-06` (before interest capitalization):
      $-980.00 (\text{Day 5 Closing}) + 620.00 (\text{E8}) =$ **-360.00 AED**
    * Active Holds: 0.00 AED
    * Available Balance: **-360.00 AED**
* **End-of-Day EOD Assessment**:
    * Overdraft Fee Assessment: Closing Balance (-360.00 AED) < 0.00 → **OVERDRAFT_FEE of 25.00 AED assessed** (`FEE-2026-01-06`)
    * Post-Fee Closing Balance: **-385.00 AED**
    * Daily Interest Accrual: Balance < 0.00 → **0.00 AED**

---

## Window-End Interest Capitalization Summary

Accumulated daily rounded interest accruals across Days 1 through 6:

| Date | Closing Ledger Balance (pre-accrual) | Daily Interest Rate | Accrued Interest (AED) |
| :--- | :--- | :--- | :--- |
| **Day 1 (`2026-01-01`)** | 1,200.00 AED | 0.04% (0.0004) | **0.48 AED** |
| **Day 2 (`2026-01-02`)** | 250.00 AED | 0.04% (0.0004) | **0.10 AED** |
| **Day 3 (`2026-01-03`)** | 250.00 AED | 0.04% (0.0004) | **0.10 AED** |
| **Day 4 (`2026-01-04`)** | 650.00 AED | 0.04% (0.0004) | **0.26 AED** |
| **Day 5 (`2026-01-05`)** | -955.00 AED | 0.04% (0.0004) | **0.00 AED** |
| **Day 6 (`2026-01-06`)** | -360.00 AED | 0.04% (0.0004) | **0.00 AED** |
| **TOTAL CAPITALIZED** | — | — | **0.94 AED** |

* **Final Interest Capitalization Entry**: Entry `INT-CAP-2026-01-06` posted as `INTEREST_CREDIT` for **0.94 AED**.
* **Final Net Account Position at Window Close**: $-385.00 + 0.94 =$ **-384.06 AED**.