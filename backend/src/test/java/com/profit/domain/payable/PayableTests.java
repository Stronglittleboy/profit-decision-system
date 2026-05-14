package com.profit.domain.payable;

import com.profit.common.exception.BusinessException;
import com.profit.domain.common.PaymentStatus;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class PayableTests {

    private Payable newP() {
        return Payable.create("AP-001", 1L, null, new BigDecimal("20000"), LocalDate.of(2026, 7, 31), null);
    }

    @Test void create_defaults() {
        Payable p = newP();
        assertEquals(PaymentStatus.PENDING, p.getStatus());
        assertEquals(new BigDecimal("20000"), p.getRemaining());
    }

    @Test void recordPayment_partial() {
        Payable p = newP();
        p.recordPayment(new BigDecimal("5000"));
        assertEquals(PaymentStatus.PARTIAL, p.getStatus());
        assertEquals(new BigDecimal("15000"), p.getRemaining());
    }

    @Test void recordPayment_full() {
        Payable p = newP();
        p.recordPayment(new BigDecimal("20000"));
        assertEquals(PaymentStatus.PAID, p.getStatus());
    }

    @Test void recordPayment_exceeds_throws() {
        Payable p = newP();
        assertThrows(BusinessException.class, () -> p.recordPayment(new BigDecimal("20001")));
    }

    @Test void recordPayment_on_paid_throws() {
        Payable p = newP();
        p.recordPayment(new BigDecimal("20000"));
        assertThrows(BusinessException.class, () -> p.recordPayment(BigDecimal.ONE));
    }

    @Test void markOverdue_works() {
        Payable p = newP();
        p.markOverdue();
        assertEquals(PaymentStatus.OVERDUE, p.getStatus());
    }

    @Test void markOverdue_on_paid_throws() {
        Payable p = newP();
        p.recordPayment(new BigDecimal("20000"));
        assertThrows(BusinessException.class, p::markOverdue);
    }
}
