package com.profit.infrastructure.counterparty;

import com.profit.domain.counterparty.Counterparty;
import com.profit.domain.counterparty.CounterpartyType;
import com.profit.domain.counterparty.CreditLevel;

public class CounterpartyConverter {

    private CounterpartyConverter() {}

    public static Counterparty toDomain(CounterpartyEntity entity) {
        return Counterparty.reconstruct(
                entity.getId(),
                entity.getName(),
                CounterpartyType.fromCode(entity.getType()),
                entity.getContact(),
                entity.getPhone(),
                entity.getAddress(),
                entity.getTaxNo(),
                CreditLevel.fromCode(entity.getCreditLevel()),
                entity.getEnabled(),
                entity.getRemark(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static CounterpartyEntity toEntity(Counterparty domain) {
        CounterpartyEntity entity = new CounterpartyEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setType(domain.getType().getCode());
        entity.setContact(domain.getContact());
        entity.setPhone(domain.getPhone());
        entity.setAddress(domain.getAddress());
        entity.setTaxNo(domain.getTaxNo());
        entity.setCreditLevel(domain.getCreditLevel() != null ? domain.getCreditLevel().getCode() : null);
        entity.setEnabled(domain.isEnabled());
        entity.setRemark(domain.getRemark());
        return entity;
    }
}
