package com.profit.application;

import com.profit.common.exception.BusinessException;
import com.profit.domain.budget.*;
import com.profit.dto.BudgetDTO;
import com.profit.vo.BudgetVO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetAppService {

    private final BudgetRepository repository;
    private final JdbcTemplate jdbc;

    public List<BudgetVO> list(String period, String category, String status) {
        return repository.findAll(period, category, status).stream()
                .map(BudgetVO::from).collect(Collectors.toList());
    }

    public BudgetVO getDetail(Long id) {
        return BudgetVO.from(findOrThrow(id));
    }

    @Transactional
    public BudgetVO create(BudgetDTO dto) {
        if (repository.existsByPeriodAndCategory(dto.getPeriod(), dto.getCategory())) {
            throw new BusinessException(40002, "该月份该类别预算已存在");
        }
        Budget b = Budget.create(dto.getPeriod(), BudgetCategory.fromCode(dto.getCategory()),
                dto.getPlannedAmount(), dto.getRemark());
        return BudgetVO.from(repository.save(b));
    }

    @Transactional
    public BudgetVO update(Long id, BudgetDTO dto) {
        Budget b = findOrThrow(id);
        b.updatePlanned(dto.getPlannedAmount());
        return BudgetVO.from(repository.save(b));
    }

    @Transactional
    public void approve(Long id) {
        Budget b = findOrThrow(id);
        b.approve();
        repository.save(b);
    }

    @Transactional
    public void delete(Long id) {
        findOrThrow(id);
        repository.deleteById(id);
    }

    @Transactional
    public int refreshActuals(String period) {
        YearMonth ym = YearMonth.parse(period, DateTimeFormatter.ofPattern("yyyy-MM"));
        String start = ym.atDay(1).toString();
        String end = ym.atEndOfMonth().toString();

        List<Budget> budgets = repository.findByPeriod(period);
        for (Budget b : budgets) {
            BigDecimal actual;
            if (b.getCategory() == BudgetCategory.INCOME) {
                actual = querySum("income", null, start, end);
            } else if (b.getCategory() == BudgetCategory.FIXED_COST) {
                actual = querySum("cost", "fixed", start, end);
            } else {
                actual = querySum("cost", "variable", start, end);
            }
            b.refreshActual(actual);
            repository.save(b);
        }
        return budgets.size();
    }

    private BigDecimal querySum(String type, String costCategory, String start, String end) {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM fact_event WHERE type=? AND status='valid' AND business_date BETWEEN ? AND ?";
        if (costCategory != null) {
            sql += " AND cost_category=?";
            return jdbc.queryForObject(sql, BigDecimal.class, type, start, end, costCategory);
        }
        return jdbc.queryForObject(sql, BigDecimal.class, type, start, end);
    }

    private Budget findOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new BusinessException(40401, "预算不存在"));
    }
}
