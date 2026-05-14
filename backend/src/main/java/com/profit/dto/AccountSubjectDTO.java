package com.profit.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AccountSubjectDTO {

    @NotBlank(message = "请输入科目编码")
    @Size(min = 1, max = 50, message = "科目编码长度 1-50 个字符")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "科目编码只允许字母和数字")
    private String code;

    @NotBlank(message = "请输入科目名称")
    @Size(min = 1, max = 100, message = "科目名称长度 1-100 个字符")
    private String name;

    private Long parentId;

    @NotBlank(message = "请选择科目类型")
    private String type;

    @NotBlank(message = "请选择借贷方向")
    private String debitCredit;

    @NotNull(message = "请输入排序")
    @Min(value = 1, message = "排序范围 1-9999")
    @Max(value = 9999, message = "排序范围 1-9999")
    private Integer sort;

    @Size(max = 200, message = "备注最多 200 个字符")
    private String remark;
}
