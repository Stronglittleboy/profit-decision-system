package com.profit.domain.counterparty;

import com.profit.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CounterpartyDomainServiceTests {

    @Mock
    private CounterpartyRepository repository;

    @InjectMocks
    private CounterpartyDomainService domainService;

    @Test
    void validateNameUnique_existingName_throws() {
        when(repository.existsByName("客户A")).thenReturn(true);
        assertThrows(BusinessException.class, () -> domainService.validateNameUnique("客户A"));
    }

    @Test
    void validateNameUnique_newName_passes() {
        when(repository.existsByName("新客户")).thenReturn(false);
        assertDoesNotThrow(() -> domainService.validateNameUnique("新客户"));
    }

    @Test
    void validateNameUniqueForUpdate_sameEntity_passes() {
        when(repository.existsByNameAndIdNot("客户A", 1L)).thenReturn(false);
        assertDoesNotThrow(() -> domainService.validateNameUniqueForUpdate("客户A", 1L));
    }

    @Test
    void validateNameUniqueForUpdate_conflictWithOther_throws() {
        when(repository.existsByNameAndIdNot("客户A", 2L)).thenReturn(true);
        assertThrows(BusinessException.class, () -> domainService.validateNameUniqueForUpdate("客户A", 2L));
    }

    @Test
    void counterparty_create_setsDefaults() {
        Counterparty cp = Counterparty.create(
                "客户A", CounterpartyType.CUSTOMER, "张三", "13800000001",
                "北京", "123456", CreditLevel.A, "备注"
        );
        assertEquals("客户A", cp.getName());
        assertEquals(CounterpartyType.CUSTOMER, cp.getType());
        assertTrue(cp.isEnabled());
        assertEquals(CreditLevel.A, cp.getCreditLevel());
    }

    @Test
    void counterparty_update_changesFields() {
        Counterparty cp = Counterparty.create(
                "客户A", CounterpartyType.CUSTOMER, null, null,
                null, null, null, null
        );
        cp.update("供应商X", CounterpartyType.SUPPLIER, "王五", "13900000000",
                "上海", "654321", CreditLevel.B, "新备注");

        assertEquals("供应商X", cp.getName());
        assertEquals(CounterpartyType.SUPPLIER, cp.getType());
        assertEquals(CreditLevel.B, cp.getCreditLevel());
    }

    @Test
    void counterparty_enableDisable() {
        Counterparty cp = Counterparty.create(
                "客户A", CounterpartyType.CUSTOMER, null, null,
                null, null, null, null
        );
        assertTrue(cp.isEnabled());
        cp.disable();
        assertFalse(cp.isEnabled());
        cp.enable();
        assertTrue(cp.isEnabled());
    }

    @Test
    void counterpartyType_fromCode_valid() {
        assertEquals(CounterpartyType.CUSTOMER, CounterpartyType.fromCode("customer"));
        assertEquals(CounterpartyType.SUPPLIER, CounterpartyType.fromCode("supplier"));
        assertEquals(CounterpartyType.BOTH, CounterpartyType.fromCode("both"));
    }

    @Test
    void counterpartyType_fromCode_invalid_throws() {
        assertThrows(BusinessException.class, () -> CounterpartyType.fromCode("invalid"));
    }

    @Test
    void creditLevel_fromCode_nullReturnsNull() {
        assertNull(CreditLevel.fromCode(null));
        assertNull(CreditLevel.fromCode(""));
    }

    @Test
    void creditLevel_fromCode_valid() {
        assertEquals(CreditLevel.A, CreditLevel.fromCode("A"));
        assertEquals(CreditLevel.D, CreditLevel.fromCode("D"));
    }

    @Test
    void creditLevel_fromCode_invalid_throws() {
        assertThrows(BusinessException.class, () -> CreditLevel.fromCode("X"));
    }
}
