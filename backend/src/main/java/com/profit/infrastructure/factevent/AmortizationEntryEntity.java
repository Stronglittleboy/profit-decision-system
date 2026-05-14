package com.profit.infrastructure.factevent;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("amortization_entry")
public class AmortizationEntryEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long factEventId;
    private String period;
    private BigDecimal amount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
