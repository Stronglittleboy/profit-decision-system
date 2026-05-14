package com.profit.vo;

import com.profit.domain.common.PaymentRecord;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PaymentRecordVO {
    private Long id;
    private BigDecimal amount;
    private LocalDate payDate;
    private String remark;
    private LocalDateTime createdAt;

    public static PaymentRecordVO from(PaymentRecord r) {
        PaymentRecordVO vo = new PaymentRecordVO();
        vo.setId(r.getId()); vo.setAmount(r.getAmount());
        vo.setPayDate(r.getPayDate()); vo.setRemark(r.getRemark());
        vo.setCreatedAt(r.getCreatedAt());
        return vo;
    }
}
