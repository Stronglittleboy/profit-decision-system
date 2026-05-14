package com.profit.domain.common;

import java.util.List;

public interface PaymentRecordRepository {
    PaymentRecord save(PaymentRecord record);
    List<PaymentRecord> findByBiz(String bizType, Long bizId);
}
