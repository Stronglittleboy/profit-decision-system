package com.profit.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JdbcTemplate jdbc;

    public DashboardSummary getSummary() {
        DashboardSummary s = new DashboardSummary();

        BigDecimal totalIncome = queryAmount("SELECT COALESCE(SUM(amount),0) FROM fact_event WHERE type='income' AND status='valid'");
        BigDecimal totalCost = queryAmount("SELECT COALESCE(SUM(amount),0) FROM fact_event WHERE type='cost' AND status='valid'");
        s.setTotalIncome(totalIncome);
        s.setTotalCost(totalCost);
        s.setTotalProfit(totalIncome.subtract(totalCost));
        s.setProfitRate(totalIncome.compareTo(BigDecimal.ZERO) > 0
                ? s.getTotalProfit().multiply(BigDecimal.valueOf(100)).divide(totalIncome, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);

        YearMonth now = YearMonth.now();
        String monthStart = now.atDay(1).toString();
        String monthEnd = now.atEndOfMonth().toString();
        BigDecimal mi = queryAmount("SELECT COALESCE(SUM(amount),0) FROM fact_event WHERE type='income' AND status='valid' AND business_date BETWEEN ? AND ?", monthStart, monthEnd);
        BigDecimal mc = queryAmount("SELECT COALESCE(SUM(amount),0) FROM fact_event WHERE type='cost' AND status='valid' AND business_date BETWEEN ? AND ?", monthStart, monthEnd);
        s.setMonthIncome(mi);
        s.setMonthCost(mc);
        s.setMonthProfit(mi.subtract(mc));

        s.setProjectCount(queryInt("SELECT COUNT(*) FROM project"));
        s.setActiveProjectCount(queryInt("SELECT COUNT(*) FROM project WHERE status='executing'"));
        s.setContractCount(queryInt("SELECT COUNT(*) FROM contract"));
        s.setActiveContractCount(queryInt("SELECT COUNT(*) FROM contract WHERE status='active'"));

        s.setReceivableRemaining(queryAmount("SELECT COALESCE(SUM(amount - paid_amount),0) FROM receivable WHERE status!='paid'"));
        s.setPayableRemaining(queryAmount("SELECT COALESCE(SUM(amount - paid_amount),0) FROM payable WHERE status!='paid'"));
        s.setOverdueReceivableCount(queryInt("SELECT COUNT(*) FROM receivable WHERE status='overdue'"));
        s.setOverduePayableCount(queryInt("SELECT COUNT(*) FROM payable WHERE status='overdue'"));

        s.setMonthTrends(buildMonthTrends());
        s.setTopCustomers(buildTopCustomers());

        return s;
    }

    private List<DashboardSummary.MonthTrend> buildMonthTrends() {
        List<DashboardSummary.MonthTrend> trends = new ArrayList<>();
        YearMonth cur = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = cur.minusMonths(i);
            String start = ym.atDay(1).toString();
            String end = ym.atEndOfMonth().toString();
            BigDecimal inc = queryAmount("SELECT COALESCE(SUM(amount),0) FROM fact_event WHERE type='income' AND status='valid' AND business_date BETWEEN ? AND ?", start, end);
            BigDecimal cost = queryAmount("SELECT COALESCE(SUM(amount),0) FROM fact_event WHERE type='cost' AND status='valid' AND business_date BETWEEN ? AND ?", start, end);
            DashboardSummary.MonthTrend t = new DashboardSummary.MonthTrend();
            t.setMonth(ym.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            t.setIncome(inc);
            t.setCost(cost);
            t.setProfit(inc.subtract(cost));
            trends.add(t);
        }
        return trends;
    }

    private List<DashboardSummary.TopCustomer> buildTopCustomers() {
        String sql = """
            SELECT fe.counterparty_id, cp.name,
                   COALESCE(SUM(CASE WHEN fe.type='income' THEN fe.amount ELSE 0 END),0) AS income,
                   COALESCE(SUM(CASE WHEN fe.type='cost' THEN fe.amount ELSE 0 END),0) AS cost
            FROM fact_event fe
            LEFT JOIN counterparty cp ON cp.id = fe.counterparty_id
            WHERE fe.status = 'valid'
            GROUP BY fe.counterparty_id, cp.name
            ORDER BY income DESC
            LIMIT 5
            """;
        return jdbc.query(sql, (rs, i) -> {
            DashboardSummary.TopCustomer c = new DashboardSummary.TopCustomer();
            c.setCounterpartyId(rs.getLong("counterparty_id"));
            c.setCounterpartyName(rs.getString("name"));
            c.setIncome(rs.getBigDecimal("income"));
            c.setCost(rs.getBigDecimal("cost"));
            c.setProfit(c.getIncome().subtract(c.getCost()));
            return c;
        });
    }

    private BigDecimal queryAmount(String sql, Object... args) {
        return jdbc.queryForObject(sql, BigDecimal.class, args);
    }

    private int queryInt(String sql) {
        Integer r = jdbc.queryForObject(sql, Integer.class);
        return r != null ? r : 0;
    }
}
