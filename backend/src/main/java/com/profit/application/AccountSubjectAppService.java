package com.profit.application;

import com.profit.common.exception.BusinessException;
import com.profit.domain.accountsubject.*;
import com.profit.dto.AccountSubjectDTO;
import com.profit.vo.AccountSubjectVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountSubjectAppService {

    private final AccountSubjectRepository repository;
    private final AccountSubjectDomainService domainService;

    @Transactional
    public AccountSubjectVO createSubject(AccountSubjectDTO dto) {
        domainService.validateCodeUnique(dto.getCode());

        AccountSubjectType type = AccountSubjectType.fromCode(dto.getType());
        DebitCredit debitCredit = DebitCredit.fromCode(dto.getDebitCredit());

        if (dto.getParentId() != null) {
            domainService.validateParentExists(dto.getParentId());
        }

        AccountSubject subject = AccountSubject.create(
                dto.getCode(), dto.getName(), dto.getParentId(),
                type, debitCredit, dto.getSort(), dto.getRemark()
        );

        int level = domainService.resolveLevel(dto.getParentId());
        if (subject.isRoot()) {
            subject.setLevelAsRoot();
        } else {
            subject.calculateLevel(level - 1);
        }

        AccountSubject saved = repository.save(subject);
        return AccountSubjectVO.from(saved);
    }

    @Transactional
    public AccountSubjectVO updateSubject(Long id, AccountSubjectDTO dto) {
        AccountSubject subject = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "会计科目不存在"));

        AccountSubjectType type = AccountSubjectType.fromCode(dto.getType());
        DebitCredit debitCredit = DebitCredit.fromCode(dto.getDebitCredit());

        Long newParentId = dto.getParentId();
        if (newParentId != null) {
            domainService.validateNotCircular(id, newParentId);
            domainService.validateParentExists(newParentId);
        }

        subject.update(dto.getName(), newParentId, type, debitCredit, dto.getSort(), dto.getRemark());

        int level = domainService.resolveLevel(newParentId);
        if (subject.isRoot()) {
            subject.setLevelAsRoot();
        } else {
            subject.calculateLevel(level - 1);
        }

        AccountSubject saved = repository.save(subject);
        return AccountSubjectVO.from(saved);
    }

    @Transactional
    public void deleteSubject(Long id) {
        repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "会计科目不存在"));
        domainService.validateNoDependentChildren(id);
        repository.deleteById(id);
    }

    @Transactional
    public void toggleStatus(Long id, boolean enabled) {
        AccountSubject subject = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "会计科目不存在"));
        if (enabled) {
            subject.enable();
        } else {
            subject.disable();
        }
        repository.save(subject);
    }

    public List<AccountSubjectTreeNode> getTree(String keyword) {
        List<AccountSubject> subjects;
        if (keyword == null || keyword.isBlank()) {
            subjects = repository.findAll();
        } else {
            subjects = domainService.searchWithAncestors(keyword);
        }
        return domainService.buildTree(subjects);
    }

    public AccountSubjectVO getDetail(Long id) {
        AccountSubject subject = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "会计科目不存在"));
        return AccountSubjectVO.from(subject);
    }
}
