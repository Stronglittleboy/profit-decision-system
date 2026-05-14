package com.profit.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentRecordDTO {
    @NotNull(message = "请输入金额")
    @DecimalMin(value = "0.01", message = "金额必须大于 0")
    private BigDecimal amount;

    @NotNull(message = "请选择日期")
    private LocalDate payDate;

    @Size(max = 500, message = "备注最多 500 字")
    private String remark;
}
