package com.profit.domain.accountsubject;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AccountSubjectTreeNode {

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
    private List<AccountSubjectTreeNode> children = new ArrayList<>();

    public static AccountSubjectTreeNode from(AccountSubject subject) {
        AccountSubjectTreeNode node = new AccountSubjectTreeNode();
        node.setId(subject.getId());
        node.setCode(subject.getCode());
        node.setName(subject.getName());
        node.setParentId(subject.getParentId());
        node.setLevel(subject.getLevel());
        node.setType(subject.getType().getCode());
        node.setTypeName(subject.getType().getLabel());
        node.setDebitCredit(subject.getDebitCredit().getCode());
        node.setDebitCreditName(subject.getDebitCredit().getLabel());
        node.setEnabled(subject.isEnabled());
        node.setSort(subject.getSort());
        node.setRemark(subject.getRemark());
        return node;
    }
}
