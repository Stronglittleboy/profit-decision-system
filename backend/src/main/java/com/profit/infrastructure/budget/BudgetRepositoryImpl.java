package com.profit.infrastructure.budget;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.profit.domain.budget.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class BudgetRepositoryImpl implements BudgetRepository {
    private final BudgetMapper mapper;

    @Override
    public Optional<Budget> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public List<Budget> findByPeriod(String period) {
        LambdaQueryWrapper<BudgetEntity> w = new LambdaQueryWrapper<>();
        w.eq(BudgetEntity::getPeriod, period);
        return mapper.selectList(w).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Budget> findAll(String period, String category, String status) {
        LambdaQueryWrapper<BudgetEntity> w = new LambdaQueryWrapper<>();
        if (period != null && !period.isBlank()) w.eq(BudgetEntity::getPeriod, period);
        if (category != null && !category.isBlank()) w.eq(BudgetEntity::getCategory, category);
        if (status != null && !status.isBlank()) w.eq(BudgetEntity::getStatus, status);
        w.orderByDesc(BudgetEntity::getPeriod).orderByAsc(BudgetEntity::getCategory);
        return mapper.selectList(w).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByPeriodAndCategory(String period, String category) {
        return mapper.selectCount(new LambdaQueryWrapper<BudgetEntity>()
                .eq(BudgetEntity::getPeriod, period).eq(BudgetEntity::getCategory, category)) > 0;
    }

    @Override
    public Budget save(Budget b) {
        BudgetEntity e = toEntity(b);
        if (e.getId() == null) mapper.insert(e);
        else mapper.updateById(e);
        return toDomain(mapper.selectById(e.getId()));
    }

    @Override
    public void deleteById(Long id) { mapper.deleteById(id); }

    private Budget toDomain(BudgetEntity e) {
        return Budget.reconstruct(e.getId(), e.getPeriod(), BudgetCategory.fromCode(e.getCategory()),
                e.getPlannedAmount(), e.getActualAmount(), BudgetStatus.fromCode(e.getStatus()),
                e.getRemark(), e.getCreatedAt(), e.getUpdatedAt());
    }

    private BudgetEntity toEntity(Budget b) {
        BudgetEntity e = new BudgetEntity();
        e.setId(b.getId()); e.setPeriod(b.getPeriod());
        e.setCategory(b.getCategory().getCode()); e.setPlannedAmount(b.getPlannedAmount());
        e.setActualAmount(b.getActualAmount()); e.setStatus(b.getStatus().getCode());
        e.setRemark(b.getRemark());
        return e;
    }
}
