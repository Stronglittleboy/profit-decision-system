package com.profit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CounterpartyDTO {

    @NotBlank(message = "请输入名称")
    @Size(min = 1, max = 100, message = "名称长度 1-100 个字符")
    private String name;

    @NotBlank(message = "请选择类型")
    private String type;

    @Size(max = 100, message = "联系人最多 100 个字符")
    private String contact;

    @Size(max = 20, message = "电话最多 20 个字符")
    private String phone;

    @Size(max = 200, message = "地址最多 200 个字符")
    private String address;

    @Size(max = 50, message = "税号最多 50 个字符")
    private String taxNo;

    private String creditLevel;

    @Size(max = 200, message = "备注最多 200 个字符")
    private String remark;
}
