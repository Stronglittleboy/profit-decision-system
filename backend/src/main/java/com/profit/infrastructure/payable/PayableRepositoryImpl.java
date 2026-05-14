package com.profit.infrastructure.payable;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.profit.domain.payable.Payable;
import com.profit.domain.payable.PayableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PayableRepositoryImpl implements PayableRepository {
    private final PayableMapper mapper;

    @Override public Optional<Payable> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(PayableConverter::toDomain);
    }

    @Override public List<Payable> search(String keyword, String status) {
        LambdaQueryWrapper<PayableEntity> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) w.like(PayableEntity::getCode, keyword);
        if (status != null && !status.isBlank()) w.eq(PayableEntity::getStatus, status);
        w.orderByDesc(PayableEntity::getCreatedAt);
        return mapper.selectList(w).stream().map(PayableConverter::toDomain).collect(Collectors.toList());
    }

    @Override public Payable save(Payable p) {
        PayableEntity e = PayableConverter.toEntity(p);
        if (e.getId() == null) mapper.insert(e); else mapper.updateById(e);
        return PayableConverter.toDomain(mapper.selectById(e.getId()));
    }

    @Override public void deleteById(Long id) { mapper.deleteById(id); }

    @Override public boolean existsByCode(String code) {
        return mapper.selectCount(new LambdaQueryWrapper<PayableEntity>().eq(PayableEntity::getCode, code)) > 0;
    }
}
