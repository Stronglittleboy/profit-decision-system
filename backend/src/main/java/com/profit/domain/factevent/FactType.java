package com.profit.domain.factevent;

import com.profit.common.exception.BusinessException;
import lombok.Getter;

@Getter
public enum FactType {

    INCOME("income", "收入"),
    COST("cost", "成本");

    private final String code;
    private final String label;

    FactType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static FactType fromCode(String code) {
        for (FactType t : values()) {
            if (t.code.equals(code)) return t;
        }
        throw new BusinessException(400, "无效的事实类型: " + code);
    }
}
