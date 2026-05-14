package com.profit.infrastructure.counterparty;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("counterparty")
public class CounterpartyEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String type;

    private String contact;

    private String phone;

    private String address;

    private String taxNo;

    private String creditLevel;

    private Boolean enabled;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
