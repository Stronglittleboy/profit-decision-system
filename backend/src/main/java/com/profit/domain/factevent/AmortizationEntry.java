package com.profit.domain.factevent;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class AmortizationEntry {
    private Long id;
    private Long factEventId;
    private String period;
    private BigDecimal amount;
    private LocalDateTime createdAt;

    private AmortizationEntry() {}

    public static AmortizationEntry create(Long factEventId, String period, BigDecimal amount) {
        AmortizationEntry e = new AmortizationEntry();
        e.factEventId = factEventId;
        e.period = period;
        e.amount = amount;
        return e;
    }

    public static AmortizationEntry reconstruct(Long id, Long factEventId, String period,
                                                BigDecimal amount, LocalDateTime createdAt) {
        AmortizationEntry e = new AmortizationEntry();
        e.id = id; e.factEventId = factEventId; e.period = period;
        e.amount = amount; e.createdAt = createdAt;
        return e;
    }
}
