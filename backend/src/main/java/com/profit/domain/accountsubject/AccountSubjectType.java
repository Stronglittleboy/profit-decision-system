package com.profit.domain.accountsubject;

import com.profit.common.exception.BusinessException;
import lombok.Getter;

@Getter
public enum AccountSubjectType {

    ASSET("asset", "资产类"),
    LIABILITY("liability", "负债类"),
    EQUITY("equity", "权益类"),
    COST("cost", "成本类"),
    PROFIT_LOSS("profit_loss", "损益类");

    private final String code;
    private final String label;

    AccountSubjectType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AccountSubjectType fromCode(String code) {
        for (AccountSubjectType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new BusinessException(400, "无效的科目类型: " + code);
    }
}
