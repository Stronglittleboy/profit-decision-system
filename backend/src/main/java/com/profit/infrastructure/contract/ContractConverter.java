package com.profit.infrastructure.contract;

import com.profit.domain.contract.Contract;
import com.profit.domain.contract.ContractStatus;
import com.profit.domain.contract.ContractType;

public class ContractConverter {

    private ContractConverter() {}

    public static Contract toDomain(ContractEntity e) {
        return Contract.reconstruct(
                e.getId(), e.getCode(), e.getName(),
                e.getCounterpartyId(), e.getProjectId(),
                ContractType.fromCode(e.getType()),
                e.getAmount(),
                e.getSignDate(), e.getStartDate(), e.getEndDate(),
                ContractStatus.fromCode(e.getStatus()),
                e.getRemark(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }

    public static ContractEntity toEntity(Contract d) {
        ContractEntity e = new ContractEntity();
        e.setId(d.getId());
        e.setCode(d.getCode());
        e.setName(d.getName());
        e.setCounterpartyId(d.getCounterpartyId());
        e.setProjectId(d.getProjectId());
        e.setType(d.getType().getCode());
        e.setAmount(d.getAmount());
        e.setSignDate(d.getSignDate());
        e.setStartDate(d.getStartDate());
        e.setEndDate(d.getEndDate());
        e.setStatus(d.getStatus().getCode());
        e.setRemark(d.getRemark());
        return e;
    }
}
