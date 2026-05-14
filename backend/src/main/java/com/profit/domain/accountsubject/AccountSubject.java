package com.profit.domain.accountsubject;

import com.profit.common.exception.BusinessException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AccountSubject {

    private Long id;
    private final String code;
    private String name;
    private Long parentId;
    private int level;
    private AccountSubjectType type;
    private DebitCredit debitCredit;
    private boolean enabled;
    private int sort;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private AccountSubject(String code) {
        this.code = code;
    }

    public static AccountSubject create(String code, String name, Long parentId,
                                        AccountSubjectType type, DebitCredit debitCredit,
                                        int sort, String remark) {
        AccountSubject subject = new AccountSubject(code);
        subject.name = name;
        subject.parentId = parentId;
        subject.type = type;
        subject.debitCredit = debitCredit;
        subject.enabled = true;
        subject.sort = sort;
        subject.remark = remark;
        return subject;
    }

    public static AccountSubject reconstruct(Long id, String code, String name, Long parentId,
                                             int level, AccountSubjectType type, DebitCredit debitCredit,
                                             boolean enabled, int sort, String remark,
                                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        AccountSubject subject = new AccountSubject(code);
        subject.id = id;
        subject.name = name;
        subject.parentId = parentId;
        subject.level = level;
        subject.type = type;
        subject.debitCredit = debitCredit;
        subject.enabled = enabled;
        subject.sort = sort;
        subject.remark = remark;
        subject.createdAt = createdAt;
        subject.updatedAt = updatedAt;
        return subject;
    }

    public void update(String name, Long parentId, AccountSubjectType type,
                       DebitCredit debitCredit, int sort, String remark) {
        this.name = name;
        this.parentId = parentId;
        this.type = type;
        this.debitCredit = debitCredit;
        this.sort = sort;
        this.remark = remark;
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public void calculateLevel(int parentLevel) {
        this.level = parentLevel + 1;
    }

    public void setLevelAsRoot() {
        this.level = 1;
    }

    public boolean isRoot() {
        return this.parentId == null;
    }

    public void validateParentNotSelf(Long newParentId) {
        if (this.id != null && this.id.equals(newParentId)) {
            throw new BusinessException(40001, "父科目不能是自身");
        }
    }
}
