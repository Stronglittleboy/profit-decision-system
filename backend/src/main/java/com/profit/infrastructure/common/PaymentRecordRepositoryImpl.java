package com.profit.infrastructure.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.profit.domain.common.PaymentRecord;
import com.profit.domain.common.PaymentRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PaymentRecordRepositoryImpl implements PaymentRecordRepository {
    private final PaymentRecordMapper mapper;

    @Override
    public PaymentRecord save(PaymentRecord record) {
        PaymentRecordEntity e = new PaymentRecordEntity();
        e.setBizType(record.getBizType());
        e.setBizId(record.getBizId());
        e.setAmount(record.getAmount());
        e.setPayDate(record.getPayDate());
        e.setRemark(record.getRemark());
        mapper.insert(e);
        return toD(mapper.selectById(e.getId()));
    }

    @Override
    public List<PaymentRecord> findByBiz(String bizType, Long bizId) {
        LambdaQueryWrapper<PaymentRecordEntity> w = new LambdaQueryWrapper<>();
        w.eq(PaymentRecordEntity::getBizType, bizType).eq(PaymentRecordEntity::getBizId, bizId)
                .orderByDesc(PaymentRecordEntity::getPayDate);
        return mapper.selectList(w).stream().map(this::toD).collect(Collectors.toList());
    }

    private PaymentRecord toD(PaymentRecordEntity e) {
        return PaymentRecord.reconstruct(e.getId(), e.getBizType(), e.getBizId(),
                e.getAmount(), e.getPayDate(), e.getRemark(), e.getCreatedAt());
    }
}
