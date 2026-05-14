package com.profit.vo;

import com.profit.domain.accountsubject.AccountSubject;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AccountSubjectVO {

    private Long id;
    private String code;
    private String name;
    private Long parentId;
    private int level;
    private String type;
    private String typeName;
    private String debitCredit;
    private String debitCreditName;
    private boolean enabled;
    private int sort;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AccountSubjectVO from(AccountSubject subject) {
        AccountSubjectVO vo = new AccountSubjectVO();
        vo.setId(subject.getId());
        vo.setCode(subject.getCode());
        vo.setName(subject.getName());
        vo.setParentId(subject.getParentId());
        vo.setLevel(subject.getLevel());
        vo.setType(subject.getType().getCode());
        vo.setTypeName(subject.getType().getLabel());
        vo.setDebitCredit(subject.getDebitCredit().getCode());
        vo.setDebitCreditName(subject.getDebitCredit().getLabel());
        vo.setEnabled(subject.isEnabled());
        vo.setSort(subject.getSort());
        vo.setRemark(subject.getRemark());
        vo.setCreatedAt(subject.getCreatedAt());
        vo.setUpdatedAt(subject.getUpdatedAt());
        return vo;
    }
}
