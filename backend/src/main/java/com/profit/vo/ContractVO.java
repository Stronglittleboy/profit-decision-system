package com.profit.vo;

import com.profit.domain.contract.Contract;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ContractVO {

    private Long id;
    private String code;
    private String name;
    private Long counterpartyId;
    private String counterpartyName;
    private Long projectId;
    private String projectName;
    private String type;
    private String typeName;
    private BigDecimal amount;
    private LocalDate signDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String statusName;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ContractVO from(Contract c) {
        ContractVO vo = new ContractVO();
        vo.setId(c.getId());
        vo.setCode(c.getCode());
        vo.setName(c.getName());
        vo.setCounterpartyId(c.getCounterpartyId());
        vo.setProjectId(c.getProjectId());
        vo.setType(c.getType().getCode());
        vo.setTypeName(c.getType().getLabel());
        vo.setAmount(c.getAmount());
        vo.setSignDate(c.getSignDate());
        vo.setStartDate(c.getStartDate());
        vo.setEndDate(c.getEndDate());
        vo.setStatus(c.getStatus().getCode());
        vo.setStatusName(c.getStatus().getLabel());
        vo.setRemark(c.getRemark());
        vo.setCreatedAt(c.getCreatedAt());
        vo.setUpdatedAt(c.getUpdatedAt());
        return vo;
    }
}
