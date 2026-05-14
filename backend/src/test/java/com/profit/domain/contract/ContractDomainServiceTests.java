package com.profit.domain.contract;

import com.profit.common.exception.BusinessException;
import com.profit.domain.counterparty.Counterparty;
import com.profit.domain.counterparty.CounterpartyRepository;
import com.profit.domain.project.Project;
import com.profit.domain.project.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractDomainServiceTests {

    @Mock private ContractRepository contractRepository;
    @Mock private CounterpartyRepository counterpartyRepository;
    @Mock private ProjectRepository projectRepository;

    private ContractDomainService service;

    @BeforeEach
    void setUp() {
        service = new ContractDomainService(contractRepository, counterpartyRepository, projectRepository);
    }

    @Test
    void validateCodeUnique_ok() {
        when(contractRepository.existsByCode("CT-001")).thenReturn(false);
        assertDoesNotThrow(() -> service.validateCodeUnique("CT-001"));
    }

    @Test
    void validateCodeUnique_duplicate() {
        when(contractRepository.existsByCode("CT-001")).thenReturn(true);
        assertThrows(BusinessException.class, () -> service.validateCodeUnique("CT-001"));
    }

    @Test
    void validateCounterpartyExists_ok() {
        when(counterpartyRepository.findById(1L)).thenReturn(Optional.of(mock(Counterparty.class)));
        assertDoesNotThrow(() -> service.validateCounterpartyExists(1L));
    }

    @Test
    void validateCounterpartyExists_missing() {
        when(counterpartyRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> service.validateCounterpartyExists(999L));
    }

    @Test
    void validateProjectExists_null_ok() {
        assertDoesNotThrow(() -> service.validateProjectExists(null));
    }

    @Test
    void validateProjectExists_found_ok() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(mock(Project.class)));
        assertDoesNotThrow(() -> service.validateProjectExists(1L));
    }

    @Test
    void validateProjectExists_missing() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> service.validateProjectExists(999L));
    }

    @Test
    void validateDateRange_valid() {
        assertDoesNotThrow(() -> service.validateDateRange(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));
    }

    @Test
    void validateDateRange_invalid() {
        assertThrows(BusinessException.class, () -> service.validateDateRange(
                LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1)));
    }
}
