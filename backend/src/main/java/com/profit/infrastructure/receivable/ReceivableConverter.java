package com.profit.infrastructure.receivable;

import com.profit.domain.common.PaymentStatus;
import com.profit.domain.receivable.Receivable;

public class ReceivableConverter {
    private ReceivableConverter() {}

    public static Receivable toDomain(ReceivableEntity e) {
        return Receivable.reconstruct(e.getId(), e.getCode(), e.getCounterpartyId(), e.getContractId(),
                e.getAmount(), e.getPaidAmount(), e.getDueDate(),
                PaymentStatus.fromCode(e.getStatus()), e.getRemark(), e.getCreatedAt(), e.getUpdatedAt());
    }

    public static ReceivableEntity toEntity(Receivable d) {
        ReceivableEntity e = new ReceivableEntity();
        e.setId(d.getId()); e.setCode(d.getCode()); e.setCounterpartyId(d.getCounterpartyId());
        e.setContractId(d.getContractId()); e.setAmount(d.getAmount()); e.setPaidAmount(d.getPaidAmount());
        e.setDueDate(d.getDueDate()); e.setStatus(d.getStatus().getCode()); e.setRemark(d.getRemark());
        return e;
    }
}
