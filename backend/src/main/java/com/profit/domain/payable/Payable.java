package com.profit.domain.payable;

import com.profit.common.exception.BusinessException;
import com.profit.domain.common.PaymentStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class Payable {

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

    private Payable() {}

    public static Payable create(String code, Long counterpartyId, Long contractId,
                                 BigDecimal amount, LocalDate dueDate, String remark) {
        Payable p = new Payable();
        p.code = code; p.counterpartyId = counterpartyId; p.contractId = contractId;
        p.amount = amount; p.paidAmount = BigDecimal.ZERO; p.dueDate = dueDate;
        p.status = PaymentStatus.PENDING; p.remark = remark;
        return p;
    }

    public static Payable reconstruct(Long id, String code, Long counterpartyId, Long contractId,
                                      BigDecimal amount, BigDecimal paidAmount, LocalDate dueDate,
                                      PaymentStatus status, String remark,
                                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        Payable p = new Payable();
        p.id = id; p.code = code; p.counterpartyId = counterpartyId; p.contractId = contractId;
        p.amount = amount; p.paidAmount = paidAmount; p.dueDate = dueDate;
        p.status = status; p.remark = remark; p.createdAt = createdAt; p.updatedAt = updatedAt;
        return p;
    }

    public void recordPayment(BigDecimal payAmount) {
        if (this.status == PaymentStatus.PAID) throw new BusinessException(40001, "该笔应付已结清");
        if (payAmount.compareTo(BigDecimal.ZERO) <= 0) throw new BusinessException(400, "付款金额必须大于 0");
        BigDecimal newPaid = this.paidAmount.add(payAmount);
        if (newPaid.compareTo(this.amount) > 0) throw new BusinessException(400, "付款金额超出应付余额");
        this.paidAmount = newPaid;
        recalcStatus();
    }

    public void markOverdue() {
        if (this.status == PaymentStatus.PAID) throw new BusinessException(40001, "已结清不可标记逾期");
        this.status = PaymentStatus.OVERDUE;
    }

    public BigDecimal getRemaining() { return this.amount.subtract(this.paidAmount); }

    private void recalcStatus() {
        if (this.paidAmount.compareTo(this.amount) >= 0) this.status = PaymentStatus.PAID;
        else if (this.paidAmount.compareTo(BigDecimal.ZERO) > 0) this.status = PaymentStatus.PARTIAL;
    }
}
