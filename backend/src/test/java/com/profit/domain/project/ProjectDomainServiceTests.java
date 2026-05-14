package com.profit.domain.project;

import com.profit.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectDomainServiceTests {

    @Mock
    private ProjectRepository repository;

    private ProjectDomainService service;

    @BeforeEach
    void setUp() {
        service = new ProjectDomainService(repository);
    }

    @Test
    void validateCodeUnique_whenUnique_noException() {
        when(repository.existsByCode("PRJ-001")).thenReturn(false);
        assertDoesNotThrow(() -> service.validateCodeUnique("PRJ-001"));
    }

    @Test
    void validateCodeUnique_whenDuplicate_throwsException() {
        when(repository.existsByCode("PRJ-001")).thenReturn(true);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.validateCodeUnique("PRJ-001"));
        assertTrue(ex.getMessage().contains("项目编号已存在"));
    }

    @Test
    void validateCodeUniqueForUpdate_whenSameId_noException() {
        when(repository.existsByCodeExcludeId("PRJ-001", 1L)).thenReturn(false);
        assertDoesNotThrow(() -> service.validateCodeUniqueForUpdate("PRJ-001", 1L));
    }

    @Test
    void validateCodeUniqueForUpdate_whenOtherId_throwsException() {
        when(repository.existsByCodeExcludeId("PRJ-001", 1L)).thenReturn(true);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.validateCodeUniqueForUpdate("PRJ-001", 1L));
        assertTrue(ex.getMessage().contains("项目编号已存在"));
    }

    @Test
    void validateDateRange_validRange_noException() {
        assertDoesNotThrow(() -> service.validateDateRange(
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 8, 31)));
    }

    @Test
    void validateDateRange_sameDates_noException() {
        LocalDate d = LocalDate.of(2026, 5, 1);
        assertDoesNotThrow(() -> service.validateDateRange(d, d));
    }

    @Test
    void validateDateRange_nullDates_noException() {
        assertDoesNotThrow(() -> service.validateDateRange(null, null));
        assertDoesNotThrow(() -> service.validateDateRange(LocalDate.now(), null));
        assertDoesNotThrow(() -> service.validateDateRange(null, LocalDate.now()));
    }

    @Test
    void validateDateRange_endBeforeStart_throwsException() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.validateDateRange(LocalDate.of(2026, 8, 31), LocalDate.of(2026, 5, 1)));
        assertTrue(ex.getMessage().contains("结束日期不能早于开始日期"));
    }
}
