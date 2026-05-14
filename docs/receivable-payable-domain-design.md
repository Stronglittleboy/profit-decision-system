# receivable / payable 领域设计

## 目标

应收账款（Receivable）和应付账款（Payable）记录企业与往来方之间的债权债务关系，是收支事实在现金流维度的延伸。两者结构对称，采用统一设计。

## 领域定位

```text
  主数据层（已落地）
  ┌──────────────────────────────────────────────────┐
  │  Counterparty ✅   Contract ✅   Project ✅        │
  └──────────────┬─────────┬───────────────────────────┘
                 │         │ 被引用
                 ▼         ▼
  事实层
  ┌──────────────────────────────────────────────────┐
  │  FactEvent ✅                                      │
  │  Receivable ◄── 本模块（应收）                     │
  │  Payable    ◄── 本模块（应付）                     │
  └──────────────────────────────────────────────────┘
```

---

## 统一模型（Receivable 与 Payable 对称）

```text
┌──────────────────────────────────────────────────────────────────┐
│                      <<Aggregate Root>>                           │
│                   Receivable / Payable                             │
├──────────────────────────────────────────────────────────────────┤
│ - id: Long                                                        │
│ - code: String              «单据编号，唯一»                      │
│ - counterpartyId: Long      «往来方（客户/供应商）»               │
│ - contractId: Long          «关联合同，可选»                      │
│ - amount: BigDecimal        «应收/应付总额»                       │
│ - paidAmount: BigDecimal    «已收/已付金额»                       │
│ - dueDate: LocalDate        «到期日»                              │
│ - status: PaymentStatus     «pending/partial/paid/overdue»        │
│ - remark: String                                                  │
│ - createdAt / updatedAt                                           │
├──────────────────────────────────────────────────────────────────┤
│ + create(...): Self                                               │
│ + reconstruct(...): Self                                          │
│ + recordPayment(amount): void  «登记回款/付款»                    │
│ + markOverdue(): void          «标记逾期»                         │
│ + recalcStatus(): void         «根据 paidAmount 自动推断状态»     │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────┐
│  <<Value Object>>    │
│   PaymentStatus      │
├──────────────────────┤
│ PENDING   (待收/待付)│
│ PARTIAL   (部分)     │
│ PAID      (已结清)   │
│ OVERDUE   (逾期)     │
└──────────────────────┘
```

---

## 领域规则

| # | 规则 | 执行层 |
|---|------|--------|
| R1 | `code` 全局唯一 | DomainService |
| R2 | `counterpartyId` 必须关联已有往来方 | DomainService |
| R3 | `contractId` 若非空须关联已有合同 | DomainService |
| R4 | `amount` > 0 | @Valid |
| R5 | `recordPayment` 金额必须 > 0 且 paidAmount + 本次 ≤ amount | Domain |
| R6 | paidAmount == amount 时自动变为 PAID | Domain.recalcStatus |
| R7 | 0 < paidAmount < amount 时自动变为 PARTIAL | Domain.recalcStatus |
| R8 | 已结清（PAID）不可再登记回款 | Domain |

---

## 数据库

### receivable

```sql
CREATE TABLE `receivable` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT,
  `code`             VARCHAR(50)   NOT NULL COMMENT '单据编号',
  `counterparty_id`  BIGINT        NOT NULL COMMENT '客户',
  `contract_id`      BIGINT        DEFAULT NULL COMMENT '关联合同',
  `amount`           DECIMAL(15,2) NOT NULL COMMENT '应收总额',
  `paid_amount`      DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '已收金额',
  `due_date`         DATE          NOT NULL COMMENT '到期日',
  `status`           VARCHAR(20)   NOT NULL DEFAULT 'pending' COMMENT 'pending/partial/paid/overdue',
  `remark`           VARCHAR(500)  DEFAULT NULL,
  `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_counterparty` (`counterparty_id`),
  KEY `idx_status` (`status`),
  KEY `idx_due_date` (`due_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应收账款';
```

### payable

```sql
CREATE TABLE `payable` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT,
  `code`             VARCHAR(50)   NOT NULL COMMENT '单据编号',
  `counterparty_id`  BIGINT        NOT NULL COMMENT '供应商',
  `contract_id`      BIGINT        DEFAULT NULL COMMENT '关联合同',
  `amount`           DECIMAL(15,2) NOT NULL COMMENT '应付总额',
  `paid_amount`      DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '已付金额',
  `due_date`         DATE          NOT NULL COMMENT '到期日',
  `status`           VARCHAR(20)   NOT NULL DEFAULT 'pending' COMMENT 'pending/partial/paid/overdue',
  `remark`           VARCHAR(500)  DEFAULT NULL,
  `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_counterparty` (`counterparty_id`),
  KEY `idx_status` (`status`),
  KEY `idx_due_date` (`due_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应付账款';
```

---

## 编码顺序

| 步骤 | 产出 |
|------|------|
| 1 | Flyway `V10` ~ `V13`（两表 DDL + 种子数据） |
| 2 | 共享值对象 `PaymentStatus` |
| 3 | Receivable 全链路（Domain → Infra → App → Controller） |
| 4 | Payable 全链路（复用 PaymentStatus，结构对称） |
| 5 | 前端两个页面 |
| 6 | 测试 |
