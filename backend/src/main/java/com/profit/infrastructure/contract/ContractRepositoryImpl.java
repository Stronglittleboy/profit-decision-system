package com.profit.infrastructure.contract;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.profit.domain.contract.Contract;
import com.profit.domain.contract.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ContractRepositoryImpl implements ContractRepository {

    private final ContractMapper mapper;

    @Override
    public Optional<Contract> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id))
                .map(ContractConverter::toDomain);
    }

    @Override
    public List<Contract> search(String keyword, String type, String status) {
        LambdaQueryWrapper<ContractEntity> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(ContractEntity::getCode, keyword)
                    .or().like(ContractEntity::getName, keyword));
        }
        if (type != null && !type.isBlank()) {
            w.eq(ContractEntity::getType, type);
        }
        if (status != null && !status.isBlank()) {
            w.eq(ContractEntity::getStatus, status);
        }
        w.orderByDesc(ContractEntity::getCreatedAt);
        return mapper.selectList(w).stream()
                .map(ContractConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Contract save(Contract contract) {
        ContractEntity entity = ContractConverter.toEntity(contract);
        if (entity.getId() == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return ContractConverter.toDomain(mapper.selectById(entity.getId()));
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        LambdaQueryWrapper<ContractEntity> w = new LambdaQueryWrapper<>();
        w.eq(ContractEntity::getCode, code);
        return mapper.selectCount(w) > 0;
    }

    @Override
    public boolean existsByCodeExcludeId(String code, Long id) {
        LambdaQueryWrapper<ContractEntity> w = new LambdaQueryWrapper<>();
        w.eq(ContractEntity::getCode, code).ne(ContractEntity::getId, id);
        return mapper.selectCount(w) > 0;
    }
}
