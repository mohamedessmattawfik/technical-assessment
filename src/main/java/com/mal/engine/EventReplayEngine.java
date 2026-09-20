package com.mal.engine;

import com.mal.domain.LedgerAccount;
import com.mal.handler.AuthorizationLedgerHandler;
import com.mal.handler.CreditLedgerHandler;
import com.mal.handler.DebitLedgerHandler;
import com.mal.handler.LedgerHandler;
import com.mal.handler.ReversalLedgerHandler;
import com.mal.handler.SettlementLedgerHandler;
import com.mal.model.EventType;
import com.mal.model.LedgerEntry;
import com.mal.policy.InterestPolicy;
import com.mal.policy.OverdraftPolicy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;

public class EventReplayEngine {
    private final Map<EventType, LedgerHandler> handlers = new EnumMap<>(EventType.class);
    private final OverdraftPolicy overdraftPolicy;
    private final InterestPolicy interestPolicy;

    private final Map<LocalDate, BigDecimal> dailyInterestAccruals = new TreeMap<>();

    public EventReplayEngine(OverdraftPolicy overdraftPolicy, InterestPolicy interestPolicy) {
        this.overdraftPolicy = overdraftPolicy;
        this.interestPolicy = interestPolicy;
        handlers.put(EventType.CREDIT, new CreditLedgerHandler());
        handlers.put(EventType.DEBIT, new DebitLedgerHandler());
        handlers.put(EventType.AUTHORIZATION, new AuthorizationLedgerHandler());
        handlers.put(EventType.SETTLEMENT, new SettlementLedgerHandler());
        handlers.put(EventType.REVERSAL, new ReversalLedgerHandler());
    }

    public void processLedger(LedgerEntry entry, LedgerAccount account) {
        LedgerHandler handler = handlers.get(entry.type());
        if (handler == null) {
            throw new UnsupportedOperationException("No handler registered for ledger of type: " + entry.type());
        }
        handler.handle(entry, account);
    }

    public void processEndOfDay(LocalDate date, LedgerAccount account) {
        BigDecimal fee = overdraftPolicy.calculateFee(account, date);
        if (fee.compareTo(BigDecimal.ZERO) > 0) {
            LedgerEntry feeEntry = new LedgerEntry(
                    "FEE-" + date,
                    account.getAccountId(),
                    EventType.OVERDRAFT_FEE,
                    fee,
                    account.getCurrency(),
                    date,
                    date,
                    null
            );
            account.appendEntry(feeEntry);
        }
        BigDecimal dailyAccrual = interestPolicy.calculateDailyAccrual(account, date);
        dailyInterestAccruals.put(date, dailyAccrual);
    }
    public void capitalizeInterestAtWindowEnd(LocalDate endOfWindowDate, LedgerAccount account) {
        BigDecimal totalAccrued = dailyInterestAccruals.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAccrued.compareTo(BigDecimal.ZERO) > 0) {
            LedgerEntry interestEntry = new LedgerEntry(
                    "INT-CAP-" + endOfWindowDate,
                    account.getAccountId(),
                    EventType.INTEREST_CREDIT,
                    totalAccrued,
                    account.getCurrency(),
                    endOfWindowDate,
                    endOfWindowDate,
                    null
            );
            account.appendEntry(interestEntry);
        }
    }

    public Map<LocalDate, BigDecimal> getDailyAccruals() {
        return Collections.unmodifiableMap(dailyInterestAccruals);
    }
}
