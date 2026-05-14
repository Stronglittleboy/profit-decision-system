package com.profit.domain.project;

import com.profit.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ProjectTests {

    @Test
    void create_sets_default_status_and_enabled() {
        Project p = Project.create("PRJ-001", "项目A", new BigDecimal("100000"),
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 8, 31), "张三", "描述");

        assertEquals(ProjectStatus.PLANNING, p.getStatus());
        assertTrue(p.getEnabled());
        assertEquals(new BigDecimal("100000"), p.getBudget());
    }

    @Test
    void create_with_null_budget_defaults_to_zero() {
        Project p = Project.create("PRJ-002", "项目B", null, null, null, null, null);
        assertEquals(BigDecimal.ZERO, p.getBudget());
    }

    @Test
    void start_from_planning_succeeds() {
        Project p = Project.create("PRJ-001", "项目A", null, null, null, null, null);
        p.start();
        assertEquals(ProjectStatus.EXECUTING, p.getStatus());
    }

    @Test
    void start_from_executing_throws() {
        Project p = Project.create("PRJ-001", "项目A", null, null, null, null, null);
        p.start();
        BusinessException ex = assertThrows(BusinessException.class, p::start);
        assertTrue(ex.getMessage().contains("不允许"));
    }

    @Test
    void complete_from_executing_succeeds() {
        Project p = Project.create("PRJ-001", "项目A", null, null, null, null, null);
        p.start();
        p.complete();
        assertEquals(ProjectStatus.COMPLETED, p.getStatus());
    }

    @Test
    void complete_from_planning_throws() {
        Project p = Project.create("PRJ-001", "项目A", null, null, null, null, null);
        assertThrows(BusinessException.class, p::complete);
    }

    @Test
    void suspend_from_executing_succeeds() {
        Project p = Project.create("PRJ-001", "项目A", null, null, null, null, null);
        p.start();
        p.suspend();
        assertEquals(ProjectStatus.SUSPENDED, p.getStatus());
    }

    @Test
    void resume_from_suspended_succeeds() {
        Project p = Project.create("PRJ-001", "项目A", null, null, null, null, null);
        p.start();
        p.suspend();
        p.resume();
        assertEquals(ProjectStatus.EXECUTING, p.getStatus());
    }

    @Test
    void resume_from_planning_throws() {
        Project p = Project.create("PRJ-001", "项目A", null, null, null, null, null);
        assertThrows(BusinessException.class, p::resume);
    }

    @Test
    void isActive_only_when_executing_and_enabled() {
        Project p = Project.create("PRJ-001", "项目A", null, null, null, null, null);
        assertFalse(p.isActive(), "planning 不算 active");

        p.start();
        assertTrue(p.isActive(), "executing + enabled = active");

        p.disable();
        assertFalse(p.isActive(), "executing + disabled ≠ active");
    }

    @Test
    void update_changes_mutable_fields() {
        Project p = Project.create("PRJ-001", "旧名", new BigDecimal("100"), null, null, null, null);
        p.update("新名", new BigDecimal("200"),
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 9, 30),
                "李四", "新描述");

        assertEquals("新名", p.getName());
        assertEquals(new BigDecimal("200"), p.getBudget());
        assertEquals("李四", p.getManager());
    }

    @Test
    void enable_disable_toggles() {
        Project p = Project.create("PRJ-001", "项目A", null, null, null, null, null);
        assertTrue(p.getEnabled());
        p.disable();
        assertFalse(p.getEnabled());
        p.enable();
        assertTrue(p.getEnabled());
    }
}
