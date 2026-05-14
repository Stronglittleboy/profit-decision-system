package com.profit.infrastructure.factevent;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.profit.domain.factevent.AmortizationEntry;
import com.profit.domain.factevent.AmortizationEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AmortizationEntryRepositoryImpl implements AmortizationEntryRepository {
    private final AmortizationEntryMapper mapper;

    @Override
    public void saveAll(List<AmortizationEntry> entries) {
        for (AmortizationEntry e : entries) {
            AmortizationEntryEntity entity = new AmortizationEntryEntity();
            entity.setFactEventId(e.getFactEventId());
            entity.setPeriod(e.getPeriod());
            entity.setAmount(e.getAmount());
            mapper.insert(entity);
        }
    }

    @Override
    public List<AmortizationEntry> findByFactEventId(Long factEventId) {
        LambdaQueryWrapper<AmortizationEntryEntity> w = new LambdaQueryWrapper<>();
        w.eq(AmortizationEntryEntity::getFactEventId, factEventId).orderByAsc(AmortizationEntryEntity::getPeriod);
        return mapper.selectList(w).stream()
                .map(e -> AmortizationEntry.reconstruct(e.getId(), e.getFactEventId(), e.getPeriod(), e.getAmount(), e.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByFactEventId(Long factEventId) {
        mapper.delete(new LambdaQueryWrapper<AmortizationEntryEntity>().eq(AmortizationEntryEntity::getFactEventId, factEventId));
    }
}
