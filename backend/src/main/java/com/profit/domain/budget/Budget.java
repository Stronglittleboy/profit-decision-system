package com.profit.domain.budget;

import com.profit.common.exception.BusinessException;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Getter
public class Budget {
    private Long id;
    private String period;
    private BudgetCategory category;
    private BigDecimal plannedAmount;
    private BigDecimal actualAmount;
    private BudgetStatus status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Budget() {}

    public static Budget create(String period, BudgetCategory category, BigDecimal plannedAmount, String remark) {
        Budget b = new Budget();
        b.period = period;
        b.category = category;
        b.plannedAmount = plannedAmount;
        b.actualAmount = BigDecimal.ZERO;
        b.status = BudgetStatus.DRAFT;
        b.remark = remark;
        return b;
    }

    public static Budget reconstruct(Long id, String period, BudgetCategory category,
                                     BigDecimal plannedAmount, BigDecimal actualAmount,
                                     BudgetStatus status, String remark,
                                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        Budget b = new Budget();
        b.id = id; b.period = period; b.category = category;
        b.plannedAmount = plannedAmount; b.actualAmount = actualAmount;
        b.status = status; b.remark = remark;
        b.createdAt = createdAt; b.updatedAt = updatedAt;
        return b;
    }

    public void approve() {
        if (this.status == BudgetStatus.APPROVED) throw new BusinessException(40001, "预算已批准");
        this.status = BudgetStatus.APPROVED;
    }

    public void updatePlanned(BigDecimal newAmount) {
        this.plannedAmount = newAmount;
    }

    public void refreshActual(BigDecimal actual) {
        this.actualAmount = actual;
    }

    public BigDecimal getExecutionRate() {
        if (plannedAmount.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return actualAmount.multiply(BigDecimal.valueOf(100)).divide(plannedAmount, 2, RoundingMode.HALF_UP);
    }

    public boolean isOverBudget() {
        return actualAmount.compareTo(plannedAmount) > 0;
    }
}
