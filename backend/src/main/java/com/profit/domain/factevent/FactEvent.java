package com.profit.domain.factevent;

import com.profit.common.exception.BusinessException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class FactEvent {

    private Long id;
    private FactType type;
    private BigDecimal amount;
    private LocalDate businessDate;
    private LocalDate accountingDate;
    private Long subjectId;
    private Long counterpartyId;
    private CostCategory costCategory;
    private LocalDate amortizeStart;
    private LocalDate amortizeEnd;
    private String amortizeMethod;
    private String invoiceNo;
    private FactStatus status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private FactEvent() {}

    public static FactEvent create(FactType type, BigDecimal amount,
                                   LocalDate businessDate, LocalDate accountingDate,
                                   Long subjectId, Long counterpartyId,
                                   CostCategory costCategory,
                                   LocalDate amortizeStart, LocalDate amortizeEnd, String amortizeMethod,
                                   String invoiceNo, String remark) {
        FactEvent event = new FactEvent();
        event.type = type;
        event.amount = amount;
        event.businessDate = businessDate;
        event.accountingDate = accountingDate != null ? accountingDate : businessDate;
        event.subjectId = subjectId;
        event.counterpartyId = counterpartyId;
        event.costCategory = (type == FactType.COST) ? costCategory : null;
        event.amortizeStart = amortizeStart;
        event.amortizeEnd = amortizeEnd;
        event.amortizeMethod = amortizeMethod;
        event.invoiceNo = invoiceNo;
        event.status = FactStatus.VALID;
        event.remark = remark;
        return event;
    }

    public boolean isAmortizable() {
        return amortizeStart != null && amortizeEnd != null && amortizeMethod != null;
    }

    public int getAmortizeMonths() {
        if (!isAmortizable()) return 0;
        return (int) (amortizeEnd.getYear() * 12 + amortizeEnd.getMonthValue()
                - amortizeStart.getYear() * 12 - amortizeStart.getMonthValue() + 1);
    }

    public static FactEvent reconstruct(Long id, FactType type, BigDecimal amount,
                                        LocalDate businessDate, LocalDate accountingDate,
                                        Long subjectId, Long counterpartyId,
                                        CostCategory costCategory,
                                        LocalDate amortizeStart, LocalDate amortizeEnd, String amortizeMethod,
                                        String invoiceNo,
                                        FactStatus status, String remark,
                                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        FactEvent event = new FactEvent();
        event.id = id;
        event.type = type;
        event.amount = amount;
        event.businessDate = businessDate;
        event.accountingDate = accountingDate;
        event.subjectId = subjectId;
        event.counterpartyId = counterpartyId;
        event.costCategory = costCategory;
        event.amortizeStart = amortizeStart;
        event.amortizeEnd = amortizeEnd;
        event.amortizeMethod = amortizeMethod;
        event.invoiceNo = invoiceNo;
        event.status = status;
        event.remark = remark;
        event.createdAt = createdAt;
        event.updatedAt = updatedAt;
        return event;
    }

    public void reverse() {
        if (this.status == FactStatus.REVERSED) {
            throw new BusinessException(40001, "该记录已冲正");
        }
        this.status = FactStatus.REVERSED;
    }

    public boolean isReversed() {
        return this.status == FactStatus.REVERSED;
    }
}
