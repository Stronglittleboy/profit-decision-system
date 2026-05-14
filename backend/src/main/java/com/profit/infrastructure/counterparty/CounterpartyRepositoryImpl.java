package com.profit.infrastructure.counterparty;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.profit.domain.counterparty.Counterparty;
import com.profit.domain.counterparty.CounterpartyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CounterpartyRepositoryImpl implements CounterpartyRepository {

    private final CounterpartyMapper mapper;

    @Override
    public Optional<Counterparty> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id))
                .map(CounterpartyConverter::toDomain);
    }

    @Override
    public List<Counterparty> findAll() {
        LambdaQueryWrapper<CounterpartyEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(CounterpartyEntity::getCreatedAt);
        return mapper.selectList(wrapper).stream()
                .map(CounterpartyConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Counterparty> search(String keyword, String type) {
        LambdaQueryWrapper<CounterpartyEntity> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(CounterpartyEntity::getName, keyword)
                    .or().like(CounterpartyEntity::getContact, keyword)
                    .or().like(CounterpartyEntity::getPhone, keyword));
        }
        if (type != null && !type.isBlank()) {
            wrapper.eq(CounterpartyEntity::getType, type);
        }
        wrapper.orderByDesc(CounterpartyEntity::getCreatedAt);
        return mapper.selectList(wrapper).stream()
                .map(CounterpartyConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByName(String name) {
        LambdaQueryWrapper<CounterpartyEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CounterpartyEntity::getName, name);
        return mapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        LambdaQueryWrapper<CounterpartyEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CounterpartyEntity::getName, name).ne(CounterpartyEntity::getId, id);
        return mapper.selectCount(wrapper) > 0;
    }

    @Override
    public Counterparty save(Counterparty counterparty) {
        CounterpartyEntity entity = CounterpartyConverter.toEntity(counterparty);
        if (entity.getId() == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return CounterpartyConverter.toDomain(mapper.selectById(entity.getId()));
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }
}
