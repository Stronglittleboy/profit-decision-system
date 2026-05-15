package com.profit.controller;

import com.profit.common.api.ApiResponse;
import com.profit.common.exception.BusinessException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private static final Pattern ISO_LOCAL_DATE = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final JdbcTemplate jdbc;

    @GetMapping("/customer-rank")
    public ApiResponse<List<CustomerRankVO>> customerRank(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        String sd = normalizeParam(startDate);
        String ed = normalizeParam(endDate);
        validateIsoDate("startDate", sd);
        validateIsoDate("endDate", ed);

        StringBuilder sql = new StringBuilder(
                """
                SELECT fe.counterparty_id, cp.name AS counterparty_name,
                       COALESCE(SUM(CASE WHEN fe.type='income' THEN fe.amount ELSE 0 END),0) AS income,
                       COALESCE(SUM(CASE WHEN fe.type='cost'   THEN fe.amount ELSE 0 END),0) AS cost
                FROM fact_event fe
                LEFT JOIN counterparty cp ON cp.id = fe.counterparty_id
                WHERE fe.status = 'valid'
                """);
        List<Object> args = new ArrayList<>();
        if (sd != null) {
            sql.append(" AND fe.business_date >= ?");
            args.add(sd);
        }
        if (ed != null) {
            sql.append(" AND fe.business_date <= ?");
            args.add(ed);
        }
        sql.append(" GROUP BY fe.counterparty_id, cp.name ORDER BY income DESC");

        List<CustomerRankVO> rows = jdbc.query(
                sql.toString(),
                (rs, i) -> {
                    CustomerRankVO vo = new CustomerRankVO();
                    vo.setCounterpartyId(rs.getLong("counterparty_id"));
                    vo.setCounterpartyName(rs.getString("counterparty_name"));
                    vo.setIncome(rs.getBigDecimal("income"));
                    vo.setCost(rs.getBigDecimal("cost"));
                    vo.setProfit(vo.getIncome().subtract(vo.getCost()));
                    vo.setProfitRate(vo.getIncome().compareTo(BigDecimal.ZERO) > 0
                            ? vo.getProfit()
                                    .multiply(BigDecimal.valueOf(100))
                                    .divide(vo.getIncome(), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO);
                    return vo;
                },
                args.toArray());
        return ApiResponse.ok(rows);
    }

    private static String normalizeParam(String raw) {
        if (raw == null) {
            return null;
        }
        String t = raw.trim();
        return t.isEmpty() ? null : t;
    }

    private static void validateIsoDate(String paramName, String value) {
        if (value == null) {
            return;
        }
        if (!ISO_LOCAL_DATE.matcher(value).matches()) {
            throw new BusinessException(400, "日期格式须为 yyyy-MM-dd：" + paramName);
        }
        try {
            LocalDate.parse(value, ISO_FMT);
        } catch (DateTimeParseException ex) {
            throw new BusinessException(400, "日期格式须为 yyyy-MM-dd：" + paramName);
        }
    }

    @Data
    public static class CustomerRankVO {
        private Long counterpartyId;
        private String counterpartyName;
        private BigDecimal income;
        private BigDecimal cost;
        private BigDecimal profit;
        private BigDecimal profitRate;
    }
}
