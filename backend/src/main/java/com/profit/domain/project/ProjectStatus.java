package com.profit.domain.project;

import com.profit.common.exception.BusinessException;
import lombok.Getter;

@Getter
public enum ProjectStatus {

    PLANNING("planning", "规划中"),
    EXECUTING("executing", "进行中"),
    COMPLETED("completed", "已完成"),
    SUSPENDED("suspended", "已暂停");

    private final String code;
    private final String label;

    ProjectStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ProjectStatus fromCode(String code) {
        for (ProjectStatus s : values()) {
            if (s.code.equals(code)) return s;
        }
        throw new BusinessException(400, "无效的项目状态: " + code);
    }
}
