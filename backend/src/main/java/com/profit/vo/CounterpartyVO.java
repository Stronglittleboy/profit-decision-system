package com.profit.vo;

import com.profit.domain.counterparty.Counterparty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CounterpartyVO {

    private Long id;
    private String name;
    private String type;
    private String typeName;
    private String contact;
    private String phone;
    private String address;
    private String taxNo;
    private String creditLevel;
    private String creditLevelName;
    private boolean enabled;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CounterpartyVO from(Counterparty cp) {
        CounterpartyVO vo = new CounterpartyVO();
        vo.setId(cp.getId());
        vo.setName(cp.getName());
        vo.setType(cp.getType().getCode());
        vo.setTypeName(cp.getType().getLabel());
        vo.setContact(cp.getContact());
        vo.setPhone(cp.getPhone());
        vo.setAddress(cp.getAddress());
        vo.setTaxNo(cp.getTaxNo());
        vo.setCreditLevel(cp.getCreditLevel() != null ? cp.getCreditLevel().getCode() : null);
        vo.setCreditLevelName(cp.getCreditLevel() != null ? cp.getCreditLevel().getLabel() : null);
        vo.setEnabled(cp.isEnabled());
        vo.setRemark(cp.getRemark());
        vo.setCreatedAt(cp.getCreatedAt());
        vo.setUpdatedAt(cp.getUpdatedAt());
        return vo;
    }
}
