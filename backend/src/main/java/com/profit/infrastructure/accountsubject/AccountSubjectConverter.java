package com.profit.infrastructure.accountsubject;

import com.profit.domain.accountsubject.AccountSubject;
import com.profit.domain.accountsubject.AccountSubjectType;
import com.profit.domain.accountsubject.DebitCredit;

public class AccountSubjectConverter {

    private AccountSubjectConverter() {}

    public static AccountSubject toDomain(AccountSubjectEntity entity) {
        return AccountSubject.reconstruct(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getParentId(),
                entity.getLevel(),
                AccountSubjectType.fromCode(entity.getType()),
                DebitCredit.fromCode(entity.getDebitCredit()),
                entity.getEnabled(),
                entity.getSort(),
                entity.getRemark(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static AccountSubjectEntity toEntity(AccountSubject domain) {
        AccountSubjectEntity entity = new AccountSubjectEntity();
        entity.setId(domain.getId());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setParentId(domain.getParentId());
        entity.setLevel(domain.getLevel());
        entity.setType(domain.getType().getCode());
        entity.setDebitCredit(domain.getDebitCredit().getCode());
        entity.setEnabled(domain.isEnabled());
        entity.setSort(domain.getSort());
        entity.setRemark(domain.getRemark());
        return entity;
    }
}
