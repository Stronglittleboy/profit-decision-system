package com.profit.domain.project;

import com.profit.common.exception.BusinessException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class Project {

    private Long id;
    private String code;
    private String name;
    private ProjectStatus status;
    private BigDecimal budget;
    private LocalDate startDate;
    private LocalDate endDate;
    private String manager;
    private String description;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Project() {}

    public static Project create(String code, String name, BigDecimal budget,
                                 LocalDate startDate, LocalDate endDate,
                                 String manager, String description) {
        Project p = new Project();
        p.code = code;
        p.name = name;
        p.status = ProjectStatus.PLANNING;
        p.budget = budget != null ? budget : BigDecimal.ZERO;
        p.startDate = startDate;
        p.endDate = endDate;
        p.manager = manager;
        p.description = description;
        p.enabled = true;
        return p;
    }

    public static Project reconstruct(Long id, String code, String name, ProjectStatus status,
                                      BigDecimal budget, LocalDate startDate, LocalDate endDate,
                                      String manager, String description, Boolean enabled,
                                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        Project p = new Project();
        p.id = id;
        p.code = code;
        p.name = name;
        p.status = status;
        p.budget = budget;
        p.startDate = startDate;
        p.endDate = endDate;
        p.manager = manager;
        p.description = description;
        p.enabled = enabled;
        p.createdAt = createdAt;
        p.updatedAt = updatedAt;
        return p;
    }

    public void update(String name, BigDecimal budget, LocalDate startDate, LocalDate endDate,
                       String manager, String description) {
        this.name = name;
        this.budget = budget != null ? budget : BigDecimal.ZERO;
        this.startDate = startDate;
        this.endDate = endDate;
        this.manager = manager;
        this.description = description;
    }

    public void start() {
        assertStatus(ProjectStatus.PLANNING, "启动");
        this.status = ProjectStatus.EXECUTING;
    }

    public void complete() {
        assertStatus(ProjectStatus.EXECUTING, "完成");
        this.status = ProjectStatus.COMPLETED;
    }

    public void suspend() {
        assertStatus(ProjectStatus.EXECUTING, "暂停");
        this.status = ProjectStatus.SUSPENDED;
    }

    public void resume() {
        assertStatus(ProjectStatus.SUSPENDED, "恢复");
        this.status = ProjectStatus.EXECUTING;
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public boolean isActive() {
        return this.status == ProjectStatus.EXECUTING && Boolean.TRUE.equals(this.enabled);
    }

    private void assertStatus(ProjectStatus expected, String action) {
        if (this.status != expected) {
            throw new BusinessException(40004,
                    String.format("当前状态「%s」不允许执行「%s」操作", this.status.getLabel(), action));
        }
    }
}
