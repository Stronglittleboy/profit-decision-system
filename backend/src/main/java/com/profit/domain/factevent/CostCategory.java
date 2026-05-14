package com.profit.domain.factevent;

import com.profit.common.exception.BusinessException;
import lombok.Getter;

@Getter
public enum CostCategory {

    FIXED("fixed", "固定成本"),
    VARIABLE("variable", "变动成本"),
    DIRECT("direct", "直接成本"),
    INDIRECT("indirect", "间接成本");

    private final String code;
    private final String label;

    CostCategory(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CostCategory fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        for (CostCategory c : values()) {
            if (c.code.equals(code)) return c;
        }
        throw new BusinessException(400, "无效的成本类别: " + code);
    }
}
