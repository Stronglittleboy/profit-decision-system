package com.profit.domain.accountsubject;

import com.profit.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountSubjectDomainService {

    private final AccountSubjectRepository repository;

    public void validateCodeUnique(String code) {
        if (repository.existsByCode(code)) {
            throw new BusinessException(40901, "科目编码已存在");
        }
    }

    public AccountSubject validateParentExists(Long parentId) {
        return repository.findById(parentId)
                .orElseThrow(() -> new BusinessException(40401, "父级科目不存在"));
    }

    public void validateNotCircular(Long id, Long newParentId) {
        if (id.equals(newParentId)) {
            throw new BusinessException(40001, "父科目不能是自身");
        }
        Set<Long> descendantIds = repository.findAllDescendantIds(id);
        if (descendantIds.contains(newParentId)) {
            throw new BusinessException(40002, "不能形成循环依赖");
        }
    }

    public void validateNoDependentChildren(Long id) {
        if (repository.hasChildren(id)) {
            throw new BusinessException(40903, "该科目下存在子科目，不能删除");
        }
    }

    public int resolveLevel(Long parentId) {
        if (parentId == null) {
            return 1;
        }
        AccountSubject parent = validateParentExists(parentId);
        return parent.getLevel() + 1;
    }

    /**
     * 将扁平列表构建为树形结构。
     * 返回的每个 TreeNode 包含完整的子节点递归。
     */
    public List<AccountSubjectTreeNode> buildTree(List<AccountSubject> subjects) {
        Map<Long, AccountSubjectTreeNode> nodeMap = new LinkedHashMap<>();
        for (AccountSubject s : subjects) {
            nodeMap.put(s.getId(), AccountSubjectTreeNode.from(s));
        }

        List<AccountSubjectTreeNode> roots = new ArrayList<>();
        for (AccountSubjectTreeNode node : nodeMap.values()) {
            if (node.getParentId() == null) {
                roots.add(node);
            } else {
                AccountSubjectTreeNode parent = nodeMap.get(node.getParentId());
                if (parent != null) {
                    parent.getChildren().add(node);
                } else {
                    roots.add(node);
                }
            }
        }
        return roots;
    }

    /**
     * 搜索时保留祖先链：先搜索匹配节点，再补齐其祖先节点。
     */
    public List<AccountSubject> searchWithAncestors(String keyword) {
        List<AccountSubject> matched = repository.search(keyword);
        if (matched.isEmpty()) {
            return matched;
        }

        List<AccountSubject> all = repository.findAll();
        Map<Long, AccountSubject> allMap = all.stream()
                .collect(Collectors.toMap(AccountSubject::getId, s -> s, (a, b) -> a, LinkedHashMap::new));

        Set<Long> resultIds = new LinkedHashSet<>();
        for (AccountSubject m : matched) {
            resultIds.add(m.getId());
            collectAncestorIds(m, allMap, resultIds);
        }

        return resultIds.stream()
                .map(allMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void collectAncestorIds(AccountSubject subject, Map<Long, AccountSubject> allMap, Set<Long> ids) {
        Long pid = subject.getParentId();
        while (pid != null && ids.add(pid)) {
            AccountSubject parent = allMap.get(pid);
            if (parent == null) break;
            pid = parent.getParentId();
        }
    }
}
