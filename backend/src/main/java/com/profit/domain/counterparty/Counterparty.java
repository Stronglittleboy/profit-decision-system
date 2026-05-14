package com.profit.domain.counterparty;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Counterparty {

    private Long id;
    private String name;
    private CounterpartyType type;
    private String contact;
    private String phone;
    private String address;
    private String taxNo;
    private CreditLevel creditLevel;
    private boolean enabled;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Counterparty() {}

    public static Counterparty create(String name, CounterpartyType type,
                                      String contact, String phone, String address,
                                      String taxNo, CreditLevel creditLevel, String remark) {
        Counterparty cp = new Counterparty();
        cp.name = name;
        cp.type = type;
        cp.contact = contact;
        cp.phone = phone;
        cp.address = address;
        cp.taxNo = taxNo;
        cp.creditLevel = creditLevel;
        cp.enabled = true;
        cp.remark = remark;
        return cp;
    }

    public static Counterparty reconstruct(Long id, String name, CounterpartyType type,
                                           String contact, String phone, String address,
                                           String taxNo, CreditLevel creditLevel,
                                           boolean enabled, String remark,
                                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        Counterparty cp = new Counterparty();
        cp.id = id;
        cp.name = name;
        cp.type = type;
        cp.contact = contact;
        cp.phone = phone;
        cp.address = address;
        cp.taxNo = taxNo;
        cp.creditLevel = creditLevel;
        cp.enabled = enabled;
        cp.remark = remark;
        cp.createdAt = createdAt;
        cp.updatedAt = updatedAt;
        return cp;
    }

    public void update(String name, CounterpartyType type,
                       String contact, String phone, String address,
                       String taxNo, CreditLevel creditLevel, String remark) {
        this.name = name;
        this.type = type;
        this.contact = contact;
        this.phone = phone;
        this.address = address;
        this.taxNo = taxNo;
        this.creditLevel = creditLevel;
        this.remark = remark;
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }
}
