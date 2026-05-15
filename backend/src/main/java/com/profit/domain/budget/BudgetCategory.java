package com.profit.domain.budget;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BudgetCategory {
    INCOME("income", "收入"),
    FIXED_COST("fixed_cost", "固定成本"),
    VARIABLE_COST("variable_cost", "变动成本");

    private final String code;
    private final String label;

    public static BudgetCategory fromCode(String code) {
        if (code == null) return null;
        for (BudgetCategory c : values()) {
            if (c.code.equals(code)) return c;
        }
        throw new IllegalArgumentException("未知预算类别: " + code);
    }
}
