# AMBIGUITIES.md — Technical & Domain Ambiguities Resolved

This document outlines key functional and domain ambiguities identified during development and documents the architectural resolutions implemented in the core engine.

---

### 1. Backdated Transactions (`E7`) and Overdraft Fee Retroactivity
* **Ambiguity**: Event `E7` was posted on Day 5 with a backdated `value_date` of Day 2, causing the historical Day 2 ledger balance to retroactively drop to -370.00 AED. The question arose whether overdraft fees for Day 2, Day 3, and Day 4 should be retroactively calculated and posted on Day 5.
* **Resolution**:
    * Accounting best practice dictates that accounting periods closed on prior days are immutable; fees are assessed based on point-in-time EOD processing.
    * Historical value dates alter interest eligibility and valuation as of target dates (`getLedgerBalance(asOfValueDate)`), but system fee assessments are posted on the processing day the backdated entry is ingested (Day 5).

---

### 2. Settlement Amount Variance vs. Authorization Hold Amount
* **Ambiguity**: Should a `SETTLEMENT` event match the exact amount of its linked `AUTHORIZATION` hold, or can it settle for a partial or greater amount?
* **Resolution**:
    * In card network processing, clearing amounts can differ from pre-authorization amounts (e.g., restaurant tips or fuel pumps).
    * `SettlementEventHandler` releases the full active hold referenced by `referenceId` (releasing earmarked available balance) and posts the exact settlement amount to the ledger journal.

---

### 3. Overdraft Policy Fee Ledger Posting & Order of Operations
* **Ambiguity**: Does an `OVERDRAFT_FEE` entry posted at EOD take effect on the same day's closing balance for interest calculation purposes?
* **Resolution**:
    * Fees are assessed based on the closing ledger balance before fee posting.
    * Once assessed, the `OVERDRAFT_FEE` `LedgerEntry` is appended to the journal with `valueDate = evaluationDate`, immediately reducing the starting ledger balance for subsequent days.