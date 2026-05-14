package com.profit.infrastructure.factevent;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("fact_event")
public class FactEventEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String type;

    private BigDecimal amount;

    private LocalDate businessDate;

    private LocalDate accountingDate;

    private Long subjectId;

    private Long counterpartyId;

    private String costCategory;

    private LocalDate amortizeStart;
    private LocalDate amortizeEnd;
    private String amortizeMethod;

    private String invoiceNo;

    private String status;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
