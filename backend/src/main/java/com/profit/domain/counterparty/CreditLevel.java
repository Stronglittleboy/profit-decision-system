package com.profit.domain.counterparty;

import com.profit.common.exception.BusinessException;
import lombok.Getter;

@Getter
public enum CreditLevel {

    A("A", "A级"),
    B("B", "B级"),
    C("C", "C级"),
    D("D", "D级");

    private final String code;
    private final String label;

    CreditLevel(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CreditLevel fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (CreditLevel cl : values()) {
            if (cl.code.equalsIgnoreCase(code)) {
                return cl;
            }
        }
        throw new BusinessException(400, "无效的信用等级: " + code);
    }
}
