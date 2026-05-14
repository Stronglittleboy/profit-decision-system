package com.profit.infrastructure.accountsubject;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.profit.domain.accountsubject.AccountSubject;
import com.profit.domain.accountsubject.AccountSubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AccountSubjectRepositoryImpl implements AccountSubjectRepository {

    private final AccountSubjectMapper mapper;

    @Override
    public Optional<AccountSubject> findById(Long id) {
        AccountSubjectEntity entity = mapper.selectById(id);
        return Optional.ofNullable(entity).map(AccountSubjectConverter::toDomain);
    }

    @Override
    public Optional<AccountSubject> findByCode(String code) {
        LambdaQueryWrapper<AccountSubjectEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccountSubjectEntity::getCode, code);
        AccountSubjectEntity entity = mapper.selectOne(wrapper);
        return Optional.ofNullable(entity).map(AccountSubjectConverter::toDomain);
    }

    @Override
    public List<AccountSubject> findAll() {
        LambdaQueryWrapper<AccountSubjectEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(AccountSubjectEntity::getSort, AccountSubjectEntity::getCode);
        return mapper.selectList(wrapper).stream()
                .map(AccountSubjectConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountSubject> findByParentId(Long parentId) {
        LambdaQueryWrapper<AccountSubjectEntity> wrapper = new LambdaQueryWrapper<>();
        if (parentId == null) {
            wrapper.isNull(AccountSubjectEntity::getParentId);
        } else {
            wrapper.eq(AccountSubjectEntity::getParentId, parentId);
        }
        wrapper.orderByAsc(AccountSubjectEntity::getSort);
        return mapper.selectList(wrapper).stream()
                .map(AccountSubjectConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Set<Long> findAllDescendantIds(Long id) {
        Set<Long> result = new HashSet<>();
        collectDescendantIds(id, result);
        return result;
    }

    private void collectDescendantIds(Long parentId, Set<Long> result) {
        List<Long> childIds = mapper.selectChildIds(parentId);
        for (Long childId : childIds) {
            result.add(childId);
            collectDescendantIds(childId, result);
        }
    }

    @Override
    public boolean existsByCode(String code) {
        LambdaQueryWrapper<AccountSubjectEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccountSubjectEntity::getCode, code);
        return mapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean hasChildren(Long id) {
        return mapper.countByParentId(id) > 0;
    }

    @Override
    public AccountSubject save(AccountSubject subject) {
        AccountSubjectEntity entity = AccountSubjectConverter.toEntity(subject);
        if (entity.getId() == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return AccountSubjectConverter.toDomain(mapper.selectById(entity.getId()));
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public List<AccountSubject> search(String keyword) {
        LambdaQueryWrapper<AccountSubjectEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.like(AccountSubjectEntity::getCode, keyword)
                .or().like(AccountSubjectEntity::getName, keyword));
        wrapper.orderByAsc(AccountSubjectEntity::getSort, AccountSubjectEntity::getCode);
        return mapper.selectList(wrapper).stream()
                .map(AccountSubjectConverter::toDomain)
                .collect(Collectors.toList());
    }
}
