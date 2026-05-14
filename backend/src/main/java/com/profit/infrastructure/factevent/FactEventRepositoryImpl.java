package com.profit.infrastructure.factevent;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.profit.domain.factevent.FactEvent;
import com.profit.domain.factevent.FactEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FactEventRepositoryImpl implements FactEventRepository {

    private final FactEventMapper mapper;

    @Override
    public Optional<FactEvent> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id))
                .map(FactEventConverter::toDomain);
    }

    @Override
    public List<FactEvent> search(String type, String status, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<FactEventEntity> w = new LambdaQueryWrapper<>();
        if (type != null && !type.isBlank()) {
            w.eq(FactEventEntity::getType, type);
        }
        if (status != null && !status.isBlank()) {
            w.eq(FactEventEntity::getStatus, status);
        }
        if (startDate != null) {
            w.ge(FactEventEntity::getBusinessDate, startDate);
        }
        if (endDate != null) {
            w.le(FactEventEntity::getBusinessDate, endDate);
        }
        w.orderByDesc(FactEventEntity::getBusinessDate, FactEventEntity::getId);
        return mapper.selectList(w).stream()
                .map(FactEventConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public FactEvent save(FactEvent event) {
        FactEventEntity entity = FactEventConverter.toEntity(event);
        if (entity.getId() == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return FactEventConverter.toDomain(mapper.selectById(entity.getId()));
    }
}
