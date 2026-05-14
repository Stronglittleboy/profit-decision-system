package com.profit.domain.contract;

import com.profit.common.exception.BusinessException;
import lombok.Getter;

@Getter
public enum ContractStatus {

    DRAFT("draft", "草稿"),
    ACTIVE("active", "生效"),
    COMPLETED("completed", "已完成"),
    TERMINATED("terminated", "已终止");

    private final String code;
    private final String label;

    ContractStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ContractStatus fromCode(String code) {
        for (ContractStatus s : values()) {
            if (s.code.equals(code)) return s;
        }
        throw new BusinessException(400, "无效的合同状态: " + code);
    }
}
