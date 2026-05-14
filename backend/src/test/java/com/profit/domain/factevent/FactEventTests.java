package com.profit.domain.factevent;

import com.profit.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FactEventTests {

    @Test
    void create_income_should_ignore_costCategory() {
        FactEvent event = FactEvent.create(
                FactType.INCOME, new BigDecimal("50000.00"),
                LocalDate.of(2026, 5, 10), null,
                1L, 1L,
                CostCategory.FIXED, null, null, null,
                "INV001", "测试收入"
        );

        assertEquals(FactType.INCOME, event.getType());
        assertEquals(new BigDecimal("50000.00"), event.getAmount());
        assertNull(event.getCostCategory(), "收入类型应忽略成本类别");
        assertEquals(FactStatus.VALID, event.getStatus());
        assertEquals(LocalDate.of(2026, 5, 10), event.getAccountingDate());
    }

    @Test
    void create_cost_should_keep_costCategory() {
        FactEvent event = FactEvent.create(
                FactType.COST, new BigDecimal("30000.00"),
                LocalDate.of(2026, 5, 12), LocalDate.of(2026, 5, 15),
                2L, 3L,
                CostCategory.VARIABLE, null, null, null,
                null, null
        );

        assertEquals(FactType.COST, event.getType());
        assertEquals(CostCategory.VARIABLE, event.getCostCategory());
        assertEquals(LocalDate.of(2026, 5, 15), event.getAccountingDate());
    }

    @Test
    void reverse_valid_event_should_succeed() {
        FactEvent event = FactEvent.create(
                FactType.COST, new BigDecimal("1000.00"),
                LocalDate.now(), null, 1L, 1L,
                null, null, null, null, null, null
        );

        assertFalse(event.isReversed());
        event.reverse();
        assertTrue(event.isReversed());
        assertEquals(FactStatus.REVERSED, event.getStatus());
    }

    @Test
    void reverse_already_reversed_should_throw() {
        FactEvent event = FactEvent.create(
                FactType.INCOME, new BigDecimal("1000.00"),
                LocalDate.now(), null, 1L, 1L,
                null, null, null, null, null, null
        );
        event.reverse();

        BusinessException ex = assertThrows(BusinessException.class, event::reverse);
        assertTrue(ex.getMessage().contains("已冲正"));
    }

    @Test
    void reconstruct_preserves_all_fields() {
        FactEvent event = FactEvent.reconstruct(
                99L, FactType.COST, new BigDecimal("12000.00"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15),
                5L, 8L,
                CostCategory.FIXED,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 1), "linear",
                "TAX-001",
                FactStatus.VALID, "年度保险费",
                null, null
        );

        assertEquals(99L, event.getId());
        assertEquals(CostCategory.FIXED, event.getCostCategory());
        assertEquals("TAX-001", event.getInvoiceNo());
        assertTrue(event.isAmortizable());
        assertEquals(12, event.getAmortizeMonths());
    }

    @Test
    void amortizable_with_12_months() {
        FactEvent event = FactEvent.create(
                FactType.COST, new BigDecimal("12000.00"),
                LocalDate.of(2026, 1, 1), null, 1L, 1L,
                CostCategory.FIXED,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 1), "linear",
                null, "年度保险"
        );

        assertTrue(event.isAmortizable());
        assertEquals(12, event.getAmortizeMonths());
    }

    @Test
    void not_amortizable_without_dates() {
        FactEvent event = FactEvent.create(
                FactType.COST, new BigDecimal("5000.00"),
                LocalDate.now(), null, 1L, 1L,
                CostCategory.VARIABLE, null, null, null, null, null
        );

        assertFalse(event.isAmortizable());
        assertEquals(0, event.getAmortizeMonths());
    }
}
