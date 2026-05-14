package com.profit.domain.receivable;

import com.profit.common.exception.BusinessException;
import com.profit.domain.common.PaymentStatus;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ReceivableTests {

    private Receivable newR() {
        return Receivable.create("AR-001", 1L, null, new BigDecimal("10000"), LocalDate.of(2026, 6, 30), null);
    }

    @Test void create_defaults() {
        Receivable r = newR();
        assertEquals(PaymentStatus.PENDING, r.getStatus());
        assertEquals(BigDecimal.ZERO, r.getPaidAmount());
        assertEquals(new BigDecimal("10000"), r.getRemaining());
    }

    @Test void recordPayment_partial() {
        Receivable r = newR();
        r.recordPayment(new BigDecimal("3000"));
        assertEquals(PaymentStatus.PARTIAL, r.getStatus());
        assertEquals(new BigDecimal("7000"), r.getRemaining());
    }

    @Test void recordPayment_full() {
        Receivable r = newR();
        r.recordPayment(new BigDecimal("10000"));
        assertEquals(PaymentStatus.PAID, r.getStatus());
        assertEquals(BigDecimal.ZERO, r.getRemaining());
    }

    @Test void recordPayment_exceeds_throws() {
        Receivable r = newR();
        assertThrows(BusinessException.class, () -> r.recordPayment(new BigDecimal("10001")));
    }

    @Test void recordPayment_zero_throws() {
        Receivable r = newR();
        assertThrows(BusinessException.class, () -> r.recordPayment(BigDecimal.ZERO));
    }

    @Test void recordPayment_on_paid_throws() {
        Receivable r = newR();
        r.recordPayment(new BigDecimal("10000"));
        assertThrows(BusinessException.class, () -> r.recordPayment(new BigDecimal("1")));
    }

    @Test void markOverdue_on_pending() {
        Receivable r = newR();
        r.markOverdue();
        assertEquals(PaymentStatus.OVERDUE, r.getStatus());
    }

    @Test void markOverdue_on_paid_throws() {
        Receivable r = newR();
        r.recordPayment(new BigDecimal("10000"));
        assertThrows(BusinessException.class, r::markOverdue);
    }

    @Test void recordPayment_after_overdue_still_works() {
        Receivable r = newR();
        r.markOverdue();
        r.recordPayment(new BigDecimal("5000"));
        assertEquals(PaymentStatus.PARTIAL, r.getStatus());
    }
}
