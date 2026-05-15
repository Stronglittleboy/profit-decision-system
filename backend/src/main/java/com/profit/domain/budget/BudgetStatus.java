package com.profit.domain.budget;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BudgetStatus {
    DRAFT("draft", "草稿"),
    APPROVED("approved", "已批准");

    private final String code;
    private final String label;

    public static BudgetStatus fromCode(String code) {
        if (code == null) return DRAFT;
        for (BudgetStatus s : values()) {
            if (s.code.equals(code)) return s;
        }
        throw new IllegalArgumentException("未知预算状态: " + code);
    }
}
