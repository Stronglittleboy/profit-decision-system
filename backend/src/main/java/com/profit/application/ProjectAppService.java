package com.profit.application;

import com.profit.common.exception.BusinessException;
import com.profit.domain.project.Project;
import com.profit.domain.project.ProjectDomainService;
import com.profit.domain.project.ProjectRepository;
import com.profit.domain.project.ProjectStatus;
import com.profit.dto.ProjectDTO;
import com.profit.vo.ProjectPnlVO;
import com.profit.vo.ProjectVO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectAppService {

    private final ProjectRepository repository;
    private final ProjectDomainService domainService;
    private final JdbcTemplate jdbc;

    public List<ProjectVO> list(String keyword, String status) {
        return repository.search(keyword, status).stream()
                .map(this::enrichVO)
                .collect(Collectors.toList());
    }

    public ProjectVO getDetail(Long id) {
        Project project = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "项目不存在"));
        return enrichVO(project);
    }

    public ProjectPnlVO getPnl(Long id) {
        Project project = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "项目不存在"));
        ProjectPnlVO pnl = new ProjectPnlVO();
        pnl.setProjectId(project.getId());
        pnl.setProjectName(project.getName());
        pnl.setBudget(project.getBudget());

        BigDecimal income = queryProjectAmount(id, "income", null);
        BigDecimal cost = queryProjectAmount(id, "cost", null);
        pnl.setTotalIncome(income);
        pnl.setTotalCost(cost);
        BigDecimal profit = income.subtract(cost);
        pnl.setTotalProfit(profit);
        pnl.setProfitRate(income.compareTo(BigDecimal.ZERO) > 0
                ? profit.multiply(BigDecimal.valueOf(100)).divide(income, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        pnl.setBudgetExecutionRate(project.getBudget() != null && project.getBudget().compareTo(BigDecimal.ZERO) > 0
                ? cost.multiply(BigDecimal.valueOf(100)).divide(project.getBudget(), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);

        List<ProjectPnlVO.CostBreakdown> breakdowns = new ArrayList<>();
        addBreakdown(breakdowns, id, "fixed", "固定成本");
        addBreakdown(breakdowns, id, "variable", "变动成本");
        pnl.setCostBreakdown(breakdowns);

        return pnl;
    }

    private ProjectVO enrichVO(Project p) {
        ProjectVO vo = ProjectVO.from(p);
        BigDecimal income = queryProjectAmount(p.getId(), "income", null);
        BigDecimal cost = queryProjectAmount(p.getId(), "cost", null);
        vo.setTotalIncome(income);
        vo.setTotalCost(cost);
        BigDecimal profit = income.subtract(cost);
        vo.setTotalProfit(profit);
        vo.setProfitRate(income.compareTo(BigDecimal.ZERO) > 0
                ? profit.multiply(BigDecimal.valueOf(100)).divide(income, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        vo.setBudgetExecutionRate(p.getBudget() != null && p.getBudget().compareTo(BigDecimal.ZERO) > 0
                ? cost.multiply(BigDecimal.valueOf(100)).divide(p.getBudget(), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        return vo;
    }

    private BigDecimal queryProjectAmount(Long projectId, String type, String costCategory) {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM fact_event WHERE project_id=? AND type=? AND status='valid'";
        if (costCategory != null) {
            sql += " AND cost_category=?";
            return jdbc.queryForObject(sql, BigDecimal.class, projectId, type, costCategory);
        }
        return jdbc.queryForObject(sql, BigDecimal.class, projectId, type);
    }

    private void addBreakdown(List<ProjectPnlVO.CostBreakdown> list, Long projectId, String cat, String catName) {
        BigDecimal amount = queryProjectAmount(projectId, "cost", cat);
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            ProjectPnlVO.CostBreakdown bd = new ProjectPnlVO.CostBreakdown();
            bd.setCategory(cat);
            bd.setCategoryName(catName);
            bd.setAmount(amount);
            list.add(bd);
        }
    }

    @Transactional
    public ProjectVO create(ProjectDTO dto) {
        domainService.validateCodeUnique(dto.getCode());
        domainService.validateDateRange(dto.getStartDate(), dto.getEndDate());

        Project project = Project.create(
                dto.getCode(), dto.getName(), dto.getBudget(),
                dto.getStartDate(), dto.getEndDate(),
                dto.getManager(), dto.getDescription()
        );
        Project saved = repository.save(project);
        return ProjectVO.from(saved);
    }

    @Transactional
    public ProjectVO update(Long id, ProjectDTO dto) {
        Project project = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "项目不存在"));
        domainService.validateCodeUniqueForUpdate(dto.getCode(), id);
        domainService.validateDateRange(dto.getStartDate(), dto.getEndDate());

        project.update(dto.getName(), dto.getBudget(),
                dto.getStartDate(), dto.getEndDate(),
                dto.getManager(), dto.getDescription());
        Project saved = repository.save(project);
        return ProjectVO.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        Project project = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "项目不存在"));
        if (project.getStatus() == ProjectStatus.COMPLETED) {
            throw new BusinessException(40005, "已完成的项目不能删除");
        }
        repository.deleteById(id);
    }

    @Transactional
    public void transition(Long id, String action) {
        Project project = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "项目不存在"));
        switch (action) {
            case "start" -> project.start();
            case "complete" -> project.complete();
            case "suspend" -> project.suspend();
            case "resume" -> project.resume();
            default -> throw new BusinessException(400, "无效的操作: " + action);
        }
        repository.save(project);
    }

    @Transactional
    public void toggleEnabled(Long id, boolean enabled) {
        Project project = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "项目不存在"));
        if (enabled) {
            project.enable();
        } else {
            project.disable();
        }
        repository.save(project);
    }
}
