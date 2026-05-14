package com.profit.domain.accountsubject;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AccountSubjectRepository {

    Optional<AccountSubject> findById(Long id);

    Optional<AccountSubject> findByCode(String code);

    List<AccountSubject> findAll();

    List<AccountSubject> findByParentId(Long parentId);

    Set<Long> findAllDescendantIds(Long id);

    boolean existsByCode(String code);

    boolean hasChildren(Long id);

    AccountSubject save(AccountSubject subject);

    void deleteById(Long id);

    List<AccountSubject> search(String keyword);
}
