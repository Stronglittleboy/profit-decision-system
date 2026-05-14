package com.profit.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProjectDTO {

    @NotBlank(message = "请输入项目编号")
    @Size(max = 50, message = "项目编号最多 50 个字符")
    private String code;

    @NotBlank(message = "请输入项目名称")
    @Size(max = 100, message = "项目名称最多 100 个字符")
    private String name;

    @NotNull(message = "请输入总预算")
    @DecimalMin(value = "0", message = "预算不能为负数")
    @Digits(integer = 13, fraction = 2, message = "预算最多 13 位整数 + 2 位小数")
    private BigDecimal budget;

    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 50, message = "项目经理最多 50 个字符")
    private String manager;

    @Size(max = 500, message = "项目描述最多 500 个字符")
    private String description;
}
