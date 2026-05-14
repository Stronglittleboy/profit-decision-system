package com.profit.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReceivableDTO {
    @NotBlank(message = "请输入单据编号")
    @Size(max = 50, message = "单据编号最多 50 个字符")
    private String code;

    @NotNull(message = "请选择客户")
    private Long counterpartyId;

    private Long contractId;

    @NotNull(message = "请输入应收金额")
    @DecimalMin(value = "0.01", message = "金额必须大于 0")
    @Digits(integer = 13, fraction = 2)
    private BigDecimal amount;

    @NotNull(message = "请选择到期日")
    private LocalDate dueDate;

    @Size(max = 500, message = "备注最多 500 个字符")
    private String remark;
}
