package com.profit.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FactEventDTO {

    @NotBlank(message = "请选择类型")
    private String type;

    @NotNull(message = "请输入金额")
    @DecimalMin(value = "0.01", message = "金额必须大于 0")
    @Digits(integer = 13, fraction = 2, message = "金额最多 13 位整数 + 2 位小数")
    private BigDecimal amount;

    @NotNull(message = "请选择业务日期")
    private LocalDate businessDate;

    private LocalDate accountingDate;

    @NotNull(message = "请选择会计科目")
    private Long subjectId;

    @NotNull(message = "请选择往来方")
    private Long counterpartyId;

    private String costCategory;

    private LocalDate amortizeStart;
    private LocalDate amortizeEnd;
    private String amortizeMethod;

    @Size(max = 50, message = "发票号最多 50 个字符")
    private String invoiceNo;

    @Size(max = 500, message = "备注最多 500 个字符")
    private String remark;
}
