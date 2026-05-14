package com.profit.infrastructure.payable;

import com.profit.domain.common.PaymentStatus;
import com.profit.domain.payable.Payable;

public class PayableConverter {
    private PayableConverter() {}

    public static Payable toDomain(PayableEntity e) {
        return Payable.reconstruct(e.getId(), e.getCode(), e.getCounterpartyId(), e.getContractId(),
                e.getAmount(), e.getPaidAmount(), e.getDueDate(),
                PaymentStatus.fromCode(e.getStatus()), e.getRemark(), e.getCreatedAt(), e.getUpdatedAt());
    }

    public static PayableEntity toEntity(Payable d) {
        PayableEntity e = new PayableEntity();
        e.setId(d.getId()); e.setCode(d.getCode()); e.setCounterpartyId(d.getCounterpartyId());
        e.setContractId(d.getContractId()); e.setAmount(d.getAmount()); e.setPaidAmount(d.getPaidAmount());
        e.setDueDate(d.getDueDate()); e.setStatus(d.getStatus().getCode()); e.setRemark(d.getRemark());
        return e;
    }
}
