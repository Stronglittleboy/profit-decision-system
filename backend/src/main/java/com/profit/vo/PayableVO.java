package com.profit.vo;

import com.profit.domain.payable.Payable;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PayableVO {
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

    public static PayableVO from(Payable p) {
        PayableVO vo = new PayableVO();
        vo.setId(p.getId()); vo.setCode(p.getCode());
        vo.setCounterpartyId(p.getCounterpartyId()); vo.setContractId(p.getContractId());
        vo.setAmount(p.getAmount()); vo.setPaidAmount(p.getPaidAmount());
        vo.setRemaining(p.getRemaining()); vo.setDueDate(p.getDueDate());
        vo.setStatus(p.getStatus().getCode()); vo.setStatusName(p.getStatus().getLabel());
        vo.setRemark(p.getRemark()); vo.setCreatedAt(p.getCreatedAt());
        return vo;
    }
}
