package com.profit.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BudgetDTO {
    @NotBlank(message = "请选择月份")
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "月份格式 yyyy-MM")
    private String period;

    @NotBlank(message = "请选择类别")
    private String category;

    @NotNull(message = "请输入预算金额")
    @DecimalMin(value = "0", message = "预算金额不能为负")
    private BigDecimal plannedAmount;

    @Size(max = 500)
    private String remark;
}
