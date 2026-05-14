package com.profit.domain.common;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class PaymentRecord {
    private Long id;
    private String bizType;
    private Long bizId;
    private BigDecimal amount;
    private LocalDate payDate;
    private String remark;
    private LocalDateTime createdAt;

    private PaymentRecord() {}

    public static PaymentRecord create(String bizType, Long bizId, BigDecimal amount, LocalDate payDate, String remark) {
        PaymentRecord r = new PaymentRecord();
        r.bizType = bizType;
        r.bizId = bizId;
        r.amount = amount;
        r.payDate = payDate;
        r.remark = remark;
        return r;
    }

    public static PaymentRecord reconstruct(Long id, String bizType, Long bizId, BigDecimal amount,
                                            LocalDate payDate, String remark, LocalDateTime createdAt) {
        PaymentRecord r = new PaymentRecord();
        r.id = id; r.bizType = bizType; r.bizId = bizId;
        r.amount = amount; r.payDate = payDate; r.remark = remark; r.createdAt = createdAt;
        return r;
    }
}
