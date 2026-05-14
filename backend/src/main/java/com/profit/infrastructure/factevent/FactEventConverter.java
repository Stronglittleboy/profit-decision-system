package com.profit.infrastructure.factevent;

import com.profit.domain.factevent.*;

public class FactEventConverter {

    private FactEventConverter() {}

    public static FactEvent toDomain(FactEventEntity e) {
        return FactEvent.reconstruct(
                e.getId(),
                FactType.fromCode(e.getType()),
                e.getAmount(),
                e.getBusinessDate(),
                e.getAccountingDate(),
                e.getSubjectId(),
                e.getCounterpartyId(),
                CostCategory.fromCode(e.getCostCategory()),
                e.getAmortizeStart(),
                e.getAmortizeEnd(),
                e.getAmortizeMethod(),
                e.getInvoiceNo(),
                FactStatus.fromCode(e.getStatus()),
                e.getRemark(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public static FactEventEntity toEntity(FactEvent d) {
        FactEventEntity e = new FactEventEntity();
        e.setId(d.getId());
        e.setType(d.getType().getCode());
        e.setAmount(d.getAmount());
        e.setBusinessDate(d.getBusinessDate());
        e.setAccountingDate(d.getAccountingDate());
        e.setSubjectId(d.getSubjectId());
        e.setCounterpartyId(d.getCounterpartyId());
        e.setCostCategory(d.getCostCategory() != null ? d.getCostCategory().getCode() : null);
        e.setAmortizeStart(d.getAmortizeStart());
        e.setAmortizeEnd(d.getAmortizeEnd());
        e.setAmortizeMethod(d.getAmortizeMethod());
        e.setInvoiceNo(d.getInvoiceNo());
        e.setStatus(d.getStatus().getCode());
        e.setRemark(d.getRemark());
        return e;
    }
}
