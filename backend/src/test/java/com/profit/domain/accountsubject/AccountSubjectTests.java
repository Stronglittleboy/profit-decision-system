package com.profit.domain.accountsubject;

import com.profit.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountSubjectTests {

    @Test
    void create_setsDefaultValues() {
        AccountSubject subject = AccountSubject.create(
                "1001", "库存现金", null,
                AccountSubjectType.ASSET, DebitCredit.DEBIT, 1, "备注"
        );

        assertEquals("1001", subject.getCode());
        assertEquals("库存现金", subject.getName());
        assertNull(subject.getParentId());
        assertEquals(AccountSubjectType.ASSET, subject.getType());
        assertEquals(DebitCredit.DEBIT, subject.getDebitCredit());
        assertTrue(subject.isEnabled());
        assertEquals(1, subject.getSort());
        assertTrue(subject.isRoot());
    }

    @Test
    void update_changesFields_butNotCode() {
        AccountSubject subject = AccountSubject.create(
                "1001", "库存现金", null,
                AccountSubjectType.ASSET, DebitCredit.DEBIT, 1, null
        );

        subject.update("银行存款", 99L, AccountSubjectType.LIABILITY, DebitCredit.CREDIT, 2, "new remark");

        assertEquals("1001", subject.getCode());
        assertEquals("银行存款", subject.getName());
        assertEquals(99L, subject.getParentId());
        assertEquals(AccountSubjectType.LIABILITY, subject.getType());
        assertEquals(DebitCredit.CREDIT, subject.getDebitCredit());
        assertEquals(2, subject.getSort());
        assertEquals("new remark", subject.getRemark());
    }

    @Test
    void enable_disable_togglesState() {
        AccountSubject subject = AccountSubject.create(
                "1001", "库存现金", null,
                AccountSubjectType.ASSET, DebitCredit.DEBIT, 1, null
        );

        assertTrue(subject.isEnabled());
        subject.disable();
        assertFalse(subject.isEnabled());
        subject.enable();
        assertTrue(subject.isEnabled());
    }

    @Test
    void calculateLevel_setsCorrectValue() {
        AccountSubject subject = AccountSubject.create(
                "100101", "子科目", 1L,
                AccountSubjectType.ASSET, DebitCredit.DEBIT, 1, null
        );

        subject.calculateLevel(1);
        assertEquals(2, subject.getLevel());

        subject.setLevelAsRoot();
        assertEquals(1, subject.getLevel());
    }

    @Test
    void validateParentNotSelf_throwsWhenSelfReference() {
        AccountSubject subject = AccountSubject.reconstruct(
                10L, "1001", "库存现金", null, 1,
                AccountSubjectType.ASSET, DebitCredit.DEBIT,
                true, 1, null, null, null
        );

        assertThrows(BusinessException.class, () -> subject.validateParentNotSelf(10L));
        assertDoesNotThrow(() -> subject.validateParentNotSelf(20L));
    }

    @Test
    void typeFromCode_validValues() {
        assertEquals(AccountSubjectType.ASSET, AccountSubjectType.fromCode("asset"));
        assertEquals(AccountSubjectType.LIABILITY, AccountSubjectType.fromCode("liability"));
        assertEquals(AccountSubjectType.EQUITY, AccountSubjectType.fromCode("equity"));
        assertEquals(AccountSubjectType.COST, AccountSubjectType.fromCode("cost"));
        assertEquals(AccountSubjectType.PROFIT_LOSS, AccountSubjectType.fromCode("profit_loss"));
    }

    @Test
    void typeFromCode_invalidValue_throws() {
        assertThrows(BusinessException.class, () -> AccountSubjectType.fromCode("invalid"));
    }

    @Test
    void debitCreditFromCode_validValues() {
        assertEquals(DebitCredit.DEBIT, DebitCredit.fromCode("debit"));
        assertEquals(DebitCredit.CREDIT, DebitCredit.fromCode("credit"));
    }

    @Test
    void debitCreditFromCode_invalidValue_throws() {
        assertThrows(BusinessException.class, () -> DebitCredit.fromCode("invalid"));
    }
}
