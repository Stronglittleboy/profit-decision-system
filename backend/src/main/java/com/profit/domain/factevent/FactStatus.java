package com.profit.domain.factevent;

import com.profit.common.exception.BusinessException;
import lombok.Getter;

@Getter
public enum FactStatus {

    VALID("valid", "有效"),
    REVERSED("reversed", "已冲正");

    private final String code;
    private final String label;

    FactStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static FactStatus fromCode(String code) {
        for (FactStatus s : values()) {
            if (s.code.equals(code)) return s;
        }
        throw new BusinessException(400, "无效的事实状态: " + code);
    }
}
