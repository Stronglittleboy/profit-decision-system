package com.profit.domain.receivable;

import com.profit.common.exception.BusinessException;
import com.profit.domain.common.PaymentStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class Receivable {

    private Long id;
    private String code;
    private Long counterpartyId;
    private Long contractId;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private LocalDate dueDate;
    private PaymentStatus status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Receivable() {}

    public static Receivable create(String code, Long counterpartyId, Long contractId,
                                    BigDecimal amount, LocalDate dueDate, String remark) {
        Receivable r = new Receivable();
        r.code = code;
        r.counterpartyId = counterpartyId;
        r.contractId = contractId;
        r.amount = amount;
        r.paidAmount = BigDecimal.ZERO;
        r.dueDate = dueDate;
        r.status = PaymentStatus.PENDING;
        r.remark = remark;
        return r;
    }

    public static Receivable reconstruct(Long id, String code, Long counterpartyId, Long contractId,
                                         BigDecimal amount, BigDecimal paidAmount, LocalDate dueDate,
                                         PaymentStatus status, String remark,
                                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        Receivable r = new Receivable();
        r.id = id;
        r.code = code;
        r.counterpartyId = counterpartyId;
        r.contractId = contractId;
        r.amount = amount;
        r.paidAmount = paidAmount;
        r.dueDate = dueDate;
        r.status = status;
        r.remark = remark;
        r.createdAt = createdAt;
        r.updatedAt = updatedAt;
        return r;
    }

    public void recordPayment(BigDecimal payAmount) {
        if (this.status == PaymentStatus.PAID) {
            throw new BusinessException(40001, "该笔应收已结清，不可重复登记");
        }
        if (payAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(400, "回款金额必须大于 0");
        }
        BigDecimal newPaid = this.paidAmount.add(payAmount);
        if (newPaid.compareTo(this.amount) > 0) {
            throw new BusinessException(400, "回款金额超出应收余额");
        }
        this.paidAmount = newPaid;
        recalcStatus();
    }

    public void markOverdue() {
        if (this.status == PaymentStatus.PAID) {
            throw new BusinessException(40001, "已结清不可标记逾期");
        }
        this.status = PaymentStatus.OVERDUE;
    }

    public BigDecimal getRemaining() {
        return this.amount.subtract(this.paidAmount);
    }

    private void recalcStatus() {
        if (this.paidAmount.compareTo(this.amount) >= 0) {
            this.status = PaymentStatus.PAID;
        } else if (this.paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.status = PaymentStatus.PARTIAL;
        }
    }
}
