package com.profit.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContractDTO {

    @NotBlank(message = "请输入合同编号")
    @Size(max = 50, message = "合同编号最多 50 个字符")
    private String code;

    @NotBlank(message = "请输入合同名称")
    @Size(max = 200, message = "合同名称最多 200 个字符")
    private String name;

    @NotNull(message = "请选择往来方")
    private Long counterpartyId;

    private Long projectId;

    @NotBlank(message = "请选择合同类型")
    private String type;

    @NotNull(message = "请输入合同金额")
    @DecimalMin(value = "0.01", message = "合同金额必须大于 0")
    @Digits(integer = 13, fraction = 2, message = "金额最多 13 位整数 + 2 位小数")
    private BigDecimal amount;

    private LocalDate signDate;
    private LocalDate startDate;
    private LocalDate endDate;

    @Size(max = 500, message = "备注最多 500 个字符")
    private String remark;
}
