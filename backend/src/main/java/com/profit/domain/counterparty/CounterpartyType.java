package com.profit.domain.counterparty;

import com.profit.common.exception.BusinessException;
import lombok.Getter;

@Getter
public enum CounterpartyType {

    CUSTOMER("customer", "客户"),
    SUPPLIER("supplier", "供应商"),
    BOTH("both", "双重");

    private final String code;
    private final String label;

    CounterpartyType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CounterpartyType fromCode(String code) {
        for (CounterpartyType t : values()) {
            if (t.code.equals(code)) {
                return t;
            }
        }
        throw new BusinessException(400, "无效的往来方类型: " + code);
    }
}
