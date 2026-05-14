package com.profit.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PayableDTO {
    @NotBlank(message = "请输入单据编号")
    @Size(max = 50)
    private String code;

    @NotNull(message = "请选择供应商")
    private Long counterpartyId;

    private Long contractId;

    @NotNull(message = "请输入应付金额")
    @DecimalMin(value = "0.01", message = "金额必须大于 0")
    @Digits(integer = 13, fraction = 2)
    private BigDecimal amount;

    @NotNull(message = "请选择到期日")
    private LocalDate dueDate;

    @Size(max = 500)
    private String remark;
}
