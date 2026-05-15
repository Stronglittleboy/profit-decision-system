package com.profit.vo;

import com.profit.domain.project.Project;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProjectVO {

    private Long id;
    private String code;
    private String name;
    private String status;
    private String statusName;
    private BigDecimal budget;
    private LocalDate startDate;
    private LocalDate endDate;
    private String manager;
    private String description;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private BigDecimal totalIncome;
    private BigDecimal totalCost;
    private BigDecimal totalProfit;
    private BigDecimal profitRate;
    private BigDecimal budgetExecutionRate;

    public static ProjectVO from(Project p) {
        ProjectVO vo = new ProjectVO();
        vo.setId(p.getId());
        vo.setCode(p.getCode());
        vo.setName(p.getName());
        vo.setStatus(p.getStatus().getCode());
        vo.setStatusName(p.getStatus().getLabel());
        vo.setBudget(p.getBudget());
        vo.setStartDate(p.getStartDate());
        vo.setEndDate(p.getEndDate());
        vo.setManager(p.getManager());
        vo.setDescription(p.getDescription());
        vo.setEnabled(p.getEnabled());
        vo.setCreatedAt(p.getCreatedAt());
        vo.setUpdatedAt(p.getUpdatedAt());
        return vo;
    }
}
