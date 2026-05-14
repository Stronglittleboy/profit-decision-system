package com.profit.vo;

import com.profit.domain.factevent.FactEvent;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FactEventVO {

    private Long id;
    private String type;
    private String typeName;
    private BigDecimal amount;
    private LocalDate businessDate;
    private LocalDate accountingDate;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private Long counterpartyId;
    private String counterpartyName;
    private String costCategory;
    private String costCategoryName;
    private String invoiceNo;
    private String status;
    private String statusName;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FactEventVO from(FactEvent e) {
        FactEventVO vo = new FactEventVO();
        vo.setId(e.getId());
        vo.setType(e.getType().getCode());
        vo.setTypeName(e.getType().getLabel());
        vo.setAmount(e.getAmount());
        vo.setBusinessDate(e.getBusinessDate());
        vo.setAccountingDate(e.getAccountingDate());
        vo.setSubjectId(e.getSubjectId());
        vo.setCounterpartyId(e.getCounterpartyId());
        vo.setCostCategory(e.getCostCategory() != null ? e.getCostCategory().getCode() : null);
        vo.setCostCategoryName(e.getCostCategory() != null ? e.getCostCategory().getLabel() : null);
        vo.setInvoiceNo(e.getInvoiceNo());
        vo.setStatus(e.getStatus().getCode());
        vo.setStatusName(e.getStatus().getLabel());
        vo.setRemark(e.getRemark());
        vo.setCreatedAt(e.getCreatedAt());
        vo.setUpdatedAt(e.getUpdatedAt());
        return vo;
    }
}
