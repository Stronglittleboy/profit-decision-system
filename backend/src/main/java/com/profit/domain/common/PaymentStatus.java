package com.profit.domain.common;

import com.profit.common.exception.BusinessException;
import lombok.Getter;

@Getter
public enum PaymentStatus {

    PENDING("pending", "待收付"),
    PARTIAL("partial", "部分"),
    PAID("paid", "已结清"),
    OVERDUE("overdue", "逾期");

    private final String code;
    private final String label;

    PaymentStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PaymentStatus fromCode(String code) {
        for (PaymentStatus s : values()) {
            if (s.code.equals(code)) return s;
        }
        throw new BusinessException(400, "无效的收付状态: " + code);
    }
}
