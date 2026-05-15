package com.profit.vo;

import com.profit.domain.budget.Budget;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BudgetVO {
    private Long id;
    private String period;
    private String category;
    private String categoryName;
    private BigDecimal plannedAmount;
    private BigDecimal actualAmount;
    private BigDecimal executionRate;
    private boolean overBudget;
    private String status;
    private String statusName;
    private String remark;
    private LocalDateTime createdAt;

    public static BudgetVO from(Budget b) {
        BudgetVO vo = new BudgetVO();
        vo.setId(b.getId()); vo.setPeriod(b.getPeriod());
        vo.setCategory(b.getCategory().getCode()); vo.setCategoryName(b.getCategory().getLabel());
        vo.setPlannedAmount(b.getPlannedAmount()); vo.setActualAmount(b.getActualAmount());
        vo.setExecutionRate(b.getExecutionRate()); vo.setOverBudget(b.isOverBudget());
        vo.setStatus(b.getStatus().getCode()); vo.setStatusName(b.getStatus().getLabel());
        vo.setRemark(b.getRemark()); vo.setCreatedAt(b.getCreatedAt());
        return vo;
    }
}
