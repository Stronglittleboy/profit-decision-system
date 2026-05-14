package com.profit.vo;

import com.profit.domain.receivable.Receivable;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReceivableVO {
    private Long id;
    private String code;
    private Long counterpartyId;
    private String counterpartyName;
    private Long contractId;
    private String contractName;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private BigDecimal remaining;
    private LocalDate dueDate;
    private String status;
    private String statusName;
    private String remark;
    private LocalDateTime createdAt;

    public static ReceivableVO from(Receivable r) {
        ReceivableVO vo = new ReceivableVO();
        vo.setId(r.getId()); vo.setCode(r.getCode());
        vo.setCounterpartyId(r.getCounterpartyId()); vo.setContractId(r.getContractId());
        vo.setAmount(r.getAmount()); vo.setPaidAmount(r.getPaidAmount());
        vo.setRemaining(r.getRemaining()); vo.setDueDate(r.getDueDate());
        vo.setStatus(r.getStatus().getCode()); vo.setStatusName(r.getStatus().getLabel());
        vo.setRemark(r.getRemark()); vo.setCreatedAt(r.getCreatedAt());
        return vo;
    }
}
