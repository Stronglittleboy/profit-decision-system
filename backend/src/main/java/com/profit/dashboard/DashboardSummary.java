package com.profit.dashboard;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardSummary {
    private BigDecimal totalIncome;
    private BigDecimal totalCost;
    private BigDecimal totalProfit;
    private BigDecimal profitRate;
    private BigDecimal monthIncome;
    private BigDecimal monthCost;
    private BigDecimal monthProfit;
    private int projectCount;
    private int activeProjectCount;
    private int contractCount;
    private int activeContractCount;
    private BigDecimal receivableRemaining;
    private BigDecimal payableRemaining;
    private int overdueReceivableCount;
    private int overduePayableCount;
    private List<MonthTrend> monthTrends;
    private List<TopCustomer> topCustomers;

    @Data
    public static class MonthTrend {
        private String month;
        private BigDecimal income;
        private BigDecimal cost;
        private BigDecimal profit;
    }

    @Data
    public static class TopCustomer {
        private Long counterpartyId;
        private String counterpartyName;
        private BigDecimal income;
        private BigDecimal cost;
        private BigDecimal profit;
    }
}
