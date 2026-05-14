package com.profit.domain.contract;

import com.profit.common.exception.BusinessException;
import lombok.Getter;

@Getter
public enum ContractType {

    SALES("sales", "销售合同"),
    PURCHASE("purchase", "采购合同"),
    SERVICE("service", "服务合同");

    private final String code;
    private final String label;

    ContractType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ContractType fromCode(String code) {
        for (ContractType t : values()) {
            if (t.code.equals(code)) return t;
        }
        throw new BusinessException(400, "无效的合同类型: " + code);
    }
}
