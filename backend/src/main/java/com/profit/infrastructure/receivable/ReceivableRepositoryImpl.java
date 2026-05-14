package com.profit.infrastructure.receivable;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.profit.domain.receivable.Receivable;
import com.profit.domain.receivable.ReceivableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ReceivableRepositoryImpl implements ReceivableRepository {
    private final ReceivableMapper mapper;

    @Override
    public Optional<Receivable> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(ReceivableConverter::toDomain);
    }

    @Override
    public List<Receivable> search(String keyword, String status) {
        LambdaQueryWrapper<ReceivableEntity> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) w.like(ReceivableEntity::getCode, keyword);
        if (status != null && !status.isBlank()) w.eq(ReceivableEntity::getStatus, status);
        w.orderByDesc(ReceivableEntity::getCreatedAt);
        return mapper.selectList(w).stream().map(ReceivableConverter::toDomain).collect(Collectors.toList());
    }

    @Override
    public Receivable save(Receivable r) {
        ReceivableEntity e = ReceivableConverter.toEntity(r);
        if (e.getId() == null) mapper.insert(e); else mapper.updateById(e);
        return ReceivableConverter.toDomain(mapper.selectById(e.getId()));
    }

    @Override
    public void deleteById(Long id) { mapper.deleteById(id); }

    @Override
    public boolean existsByCode(String code) {
        return mapper.selectCount(new LambdaQueryWrapper<ReceivableEntity>().eq(ReceivableEntity::getCode, code)) > 0;
    }
}
