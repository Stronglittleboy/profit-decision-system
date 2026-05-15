package com.profit.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProjectPnlVO {
    private Long projectId;
    private String projectName;
    private BigDecimal totalIncome;
    private BigDecimal totalCost;
    private BigDecimal totalProfit;
    private BigDecimal profitRate;
    private BigDecimal budget;
    private BigDecimal budgetExecutionRate;
    private List<CostBreakdown> costBreakdown;

    @Data
    public static class CostBreakdown {
        private String category;
        private String categoryName;
        private BigDecimal amount;
    }
}
