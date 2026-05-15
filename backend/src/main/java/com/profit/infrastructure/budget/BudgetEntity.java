package com.profit.infrastructure.budget;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("budget")
public class BudgetEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String period;
    private String category;
    private BigDecimal plannedAmount;
    private BigDecimal actualAmount;
    private String status;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
