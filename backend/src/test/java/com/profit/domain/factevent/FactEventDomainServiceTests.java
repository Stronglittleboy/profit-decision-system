package com.profit.domain.factevent;

import com.profit.common.exception.BusinessException;
import com.profit.domain.accountsubject.AccountSubject;
import com.profit.domain.accountsubject.AccountSubjectRepository;
import com.profit.domain.counterparty.Counterparty;
import com.profit.domain.counterparty.CounterpartyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FactEventDomainServiceTests {

    @Mock
    private AccountSubjectRepository subjectRepository;

    @Mock
    private CounterpartyRepository counterpartyRepository;

    private FactEventDomainService service;

    @BeforeEach
    void setUp() {
        service = new FactEventDomainService(subjectRepository, counterpartyRepository);
    }

    @Test
    void validateSubjectExists_whenExists_noException() {
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(mock(AccountSubject.class)));
        assertDoesNotThrow(() -> service.validateSubjectExists(1L));
    }

    @Test
    void validateSubjectExists_whenMissing_throwsException() {
        when(subjectRepository.findById(999L)).thenReturn(Optional.empty());
        BusinessException ex = assertThrows(BusinessException.class, () -> service.validateSubjectExists(999L));
        assertTrue(ex.getMessage().contains("会计科目不存在"));
    }

    @Test
    void validateCounterpartyExists_whenExists_noException() {
        when(counterpartyRepository.findById(1L)).thenReturn(Optional.of(mock(Counterparty.class)));
        assertDoesNotThrow(() -> service.validateCounterpartyExists(1L));
    }

    @Test
    void validateCounterpartyExists_whenMissing_throwsException() {
        when(counterpartyRepository.findById(999L)).thenReturn(Optional.empty());
        BusinessException ex = assertThrows(BusinessException.class, () -> service.validateCounterpartyExists(999L));
        assertTrue(ex.getMessage().contains("往来方不存在"));
    }

    @Test
    void validateNotAlreadyReversed_valid_noException() {
        FactEvent event = FactEvent.create(
                FactType.INCOME, new BigDecimal("100"),
                LocalDate.now(), null, 1L, 1L, null, null, null
        );
        assertDoesNotThrow(() -> service.validateNotAlreadyReversed(event));
    }

    @Test
    void validateNotAlreadyReversed_reversed_throwsException() {
        FactEvent event = FactEvent.create(
                FactType.INCOME, new BigDecimal("100"),
                LocalDate.now(), null, 1L, 1L, null, null, null
        );
        event.reverse();
        BusinessException ex = assertThrows(BusinessException.class, () -> service.validateNotAlreadyReversed(event));
        assertTrue(ex.getMessage().contains("已冲正"));
    }
}
