package com.profit.vo;

import com.profit.domain.factevent.AmortizationEntry;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AmortizationEntryVO {
    private Long id;
    private String period;
    private BigDecimal amount;

    public static AmortizationEntryVO from(AmortizationEntry e) {
        AmortizationEntryVO vo = new AmortizationEntryVO();
        vo.setId(e.getId()); vo.setPeriod(e.getPeriod()); vo.setAmount(e.getAmount());
        return vo;
    }
}
