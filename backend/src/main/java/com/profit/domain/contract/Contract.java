package com.profit.domain.contract;

import com.profit.common.exception.BusinessException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class Contract {

    private Long id;
    private String code;
    private String name;
    private Long counterpartyId;
    private Long projectId;
    private ContractType type;
    private BigDecimal amount;
    private LocalDate signDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private ContractStatus status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Contract() {}

    public static Contract create(String code, String name, Long counterpartyId, Long projectId,
                                  ContractType type, BigDecimal amount,
                                  LocalDate signDate, LocalDate startDate, LocalDate endDate,
                                  String remark) {
        Contract c = new Contract();
        c.code = code;
        c.name = name;
        c.counterpartyId = counterpartyId;
        c.projectId = projectId;
        c.type = type;
        c.amount = amount;
        c.signDate = signDate;
        c.startDate = startDate;
        c.endDate = endDate;
        c.status = ContractStatus.DRAFT;
        c.remark = remark;
        return c;
    }

    public static Contract reconstruct(Long id, String code, String name,
                                       Long counterpartyId, Long projectId,
                                       ContractType type, BigDecimal amount,
                                       LocalDate signDate, LocalDate startDate, LocalDate endDate,
                                       ContractStatus status, String remark,
                                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        Contract c = new Contract();
        c.id = id;
        c.code = code;
        c.name = name;
        c.counterpartyId = counterpartyId;
        c.projectId = projectId;
        c.type = type;
        c.amount = amount;
        c.signDate = signDate;
        c.startDate = startDate;
        c.endDate = endDate;
        c.status = status;
        c.remark = remark;
        c.createdAt = createdAt;
        c.updatedAt = updatedAt;
        return c;
    }

    public void update(String name, Long counterpartyId, Long projectId,
                       ContractType type, BigDecimal amount,
                       LocalDate signDate, LocalDate startDate, LocalDate endDate,
                       String remark) {
        assertEditable();
        this.name = name;
        this.counterpartyId = counterpartyId;
        this.projectId = projectId;
        this.type = type;
        this.amount = amount;
        this.signDate = signDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.remark = remark;
    }

    public void activate() {
        assertStatus(ContractStatus.DRAFT, "生效");
        this.status = ContractStatus.ACTIVE;
    }

    public void complete() {
        assertStatus(ContractStatus.ACTIVE, "完成");
        this.status = ContractStatus.COMPLETED;
    }

    public void terminate() {
        if (this.status != ContractStatus.DRAFT && this.status != ContractStatus.ACTIVE) {
            throw new BusinessException(40004,
                    String.format("当前状态「%s」不允许执行「终止」操作", this.status.getLabel()));
        }
        this.status = ContractStatus.TERMINATED;
    }

    public boolean isEditable() {
        return this.status == ContractStatus.DRAFT;
    }

    public boolean isDeletable() {
        return this.status == ContractStatus.DRAFT || this.status == ContractStatus.TERMINATED;
    }

    private void assertEditable() {
        if (!isEditable()) {
            throw new BusinessException(40006, "当前状态不允许编辑");
        }
    }

    private void assertStatus(ContractStatus expected, String action) {
        if (this.status != expected) {
            throw new BusinessException(40004,
                    String.format("当前状态「%s」不允许执行「%s」操作", this.status.getLabel(), action));
        }
    }
}
