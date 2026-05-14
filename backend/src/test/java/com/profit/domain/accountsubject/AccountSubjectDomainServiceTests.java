package com.profit.domain.accountsubject;

import com.profit.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountSubjectDomainServiceTests {

    @Mock
    private AccountSubjectRepository repository;

    @InjectMocks
    private AccountSubjectDomainService domainService;

    private AccountSubject rootSubject;
    private AccountSubject childSubject;

    @BeforeEach
    void setUp() {
        rootSubject = AccountSubject.reconstruct(
                1L, "1001", "库存现金", null, 1,
                AccountSubjectType.ASSET, DebitCredit.DEBIT,
                true, 1, null, null, null
        );
        childSubject = AccountSubject.reconstruct(
                2L, "100101", "库存现金-子", 1L, 2,
                AccountSubjectType.ASSET, DebitCredit.DEBIT,
                true, 1, null, null, null
        );
    }

    @Test
    void validateCodeUnique_existingCode_throws() {
        when(repository.existsByCode("1001")).thenReturn(true);
        assertThrows(BusinessException.class, () -> domainService.validateCodeUnique("1001"));
    }

    @Test
    void validateCodeUnique_newCode_passes() {
        when(repository.existsByCode("9999")).thenReturn(false);
        assertDoesNotThrow(() -> domainService.validateCodeUnique("9999"));
    }

    @Test
    void validateParentExists_found_returnsSubject() {
        when(repository.findById(1L)).thenReturn(Optional.of(rootSubject));
        AccountSubject result = domainService.validateParentExists(1L);
        assertEquals("1001", result.getCode());
    }

    @Test
    void validateParentExists_notFound_throws() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> domainService.validateParentExists(999L));
    }

    @Test
    void validateNotCircular_selfReference_throws() {
        assertThrows(BusinessException.class, () -> domainService.validateNotCircular(1L, 1L));
    }

    @Test
    void validateNotCircular_descendantAsParent_throws() {
        when(repository.findAllDescendantIds(1L)).thenReturn(Set.of(2L, 3L));
        assertThrows(BusinessException.class, () -> domainService.validateNotCircular(1L, 2L));
    }

    @Test
    void validateNotCircular_validNewParent_passes() {
        when(repository.findAllDescendantIds(1L)).thenReturn(Set.of(2L, 3L));
        assertDoesNotThrow(() -> domainService.validateNotCircular(1L, 99L));
    }

    @Test
    void validateNoDependentChildren_hasChildren_throws() {
        when(repository.hasChildren(1L)).thenReturn(true);
        assertThrows(BusinessException.class, () -> domainService.validateNoDependentChildren(1L));
    }

    @Test
    void validateNoDependentChildren_noChildren_passes() {
        when(repository.hasChildren(1L)).thenReturn(false);
        assertDoesNotThrow(() -> domainService.validateNoDependentChildren(1L));
    }

    @Test
    void resolveLevel_nullParent_returns1() {
        assertEquals(1, domainService.resolveLevel(null));
    }

    @Test
    void resolveLevel_withParent_returnsParentLevelPlus1() {
        when(repository.findById(1L)).thenReturn(Optional.of(rootSubject));
        assertEquals(2, domainService.resolveLevel(1L));
    }

    @Test
    void buildTree_flatListBecomesTree() {
        List<AccountSubject> subjects = List.of(rootSubject, childSubject);
        List<AccountSubjectTreeNode> tree = domainService.buildTree(subjects);

        assertEquals(1, tree.size());
        assertEquals("1001", tree.get(0).getCode());
        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals("100101", tree.get(0).getChildren().get(0).getCode());
    }

    @Test
    void buildTree_emptyList_returnsEmpty() {
        List<AccountSubjectTreeNode> tree = domainService.buildTree(List.of());
        assertTrue(tree.isEmpty());
    }

    @Test
    void searchWithAncestors_includesParentChain() {
        when(repository.search("子")).thenReturn(List.of(childSubject));
        when(repository.findAll()).thenReturn(List.of(rootSubject, childSubject));

        List<AccountSubject> result = domainService.searchWithAncestors("子");

        assertEquals(2, result.size());
        Set<String> codes = new HashSet<>();
        result.forEach(s -> codes.add(s.getCode()));
        assertTrue(codes.contains("100101"));
        assertTrue(codes.contains("1001"));
    }

    @Test
    void searchWithAncestors_noMatch_returnsEmpty() {
        when(repository.search("不存在")).thenReturn(List.of());
        List<AccountSubject> result = domainService.searchWithAncestors("不存在");
        assertTrue(result.isEmpty());
    }
}
