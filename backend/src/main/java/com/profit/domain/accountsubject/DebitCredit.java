package com.profit.domain.accountsubject;

import com.profit.common.exception.BusinessException;
import lombok.Getter;

@Getter
public enum DebitCredit {

    DEBIT("debit", "借"),
    CREDIT("credit", "贷");

    private final String code;
    private final String label;

    DebitCredit(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DebitCredit fromCode(String code) {
        for (DebitCredit dc : values()) {
            if (dc.code.equals(code)) {
                return dc;
            }
        }
        throw new BusinessException(400, "无效的借贷方向: " + code);
    }
}
