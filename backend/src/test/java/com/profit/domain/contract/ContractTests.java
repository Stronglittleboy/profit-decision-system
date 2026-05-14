package com.profit.domain.contract;

import com.profit.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ContractTests {

    private Contract createDraftContract() {
        return Contract.create("CT-001", "测试合同", 1L, null,
                ContractType.SALES, new BigDecimal("100000"),
                LocalDate.now(), null, null, null);
    }

    @Test
    void create_sets_draft_status() {
        Contract c = createDraftContract();
        assertEquals(ContractStatus.DRAFT, c.getStatus());
        assertTrue(c.isEditable());
        assertTrue(c.isDeletable());
    }

    @Test
    void activate_from_draft_succeeds() {
        Contract c = createDraftContract();
        c.activate();
        assertEquals(ContractStatus.ACTIVE, c.getStatus());
        assertFalse(c.isEditable());
    }

    @Test
    void activate_from_active_throws() {
        Contract c = createDraftContract();
        c.activate();
        assertThrows(BusinessException.class, c::activate);
    }

    @Test
    void complete_from_active_succeeds() {
        Contract c = createDraftContract();
        c.activate();
        c.complete();
        assertEquals(ContractStatus.COMPLETED, c.getStatus());
        assertFalse(c.isDeletable());
    }

    @Test
    void complete_from_draft_throws() {
        Contract c = createDraftContract();
        assertThrows(BusinessException.class, c::complete);
    }

    @Test
    void terminate_from_draft_succeeds() {
        Contract c = createDraftContract();
        c.terminate();
        assertEquals(ContractStatus.TERMINATED, c.getStatus());
    }

    @Test
    void terminate_from_active_succeeds() {
        Contract c = createDraftContract();
        c.activate();
        c.terminate();
        assertEquals(ContractStatus.TERMINATED, c.getStatus());
    }

    @Test
    void terminate_from_completed_throws() {
        Contract c = createDraftContract();
        c.activate();
        c.complete();
        assertThrows(BusinessException.class, c::terminate);
    }

    @Test
    void update_on_draft_succeeds() {
        Contract c = createDraftContract();
        c.update("新名称", 2L, 1L, ContractType.PURCHASE, new BigDecimal("200000"),
                null, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 12, 31), "新备注");
        assertEquals("新名称", c.getName());
        assertEquals(ContractType.PURCHASE, c.getType());
        assertEquals(new BigDecimal("200000"), c.getAmount());
    }

    @Test
    void update_on_active_throws() {
        Contract c = createDraftContract();
        c.activate();
        assertThrows(BusinessException.class, () ->
                c.update("x", 1L, null, ContractType.SALES, BigDecimal.ONE, null, null, null, null));
    }

    @Test
    void reconstruct_preserves_fields() {
        Contract c = Contract.reconstruct(99L, "CT-099", "重建合同", 1L, 2L,
                ContractType.SERVICE, new BigDecimal("50000"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30),
                ContractStatus.ACTIVE, "备注", null, null);
        assertEquals(99L, c.getId());
        assertEquals(ContractType.SERVICE, c.getType());
        assertEquals(ContractStatus.ACTIVE, c.getStatus());
    }
}
