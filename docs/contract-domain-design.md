# contract 领域设计

## 目标

合同（Contract）是主数据域的核心实体，记录企业与往来方之间的契约关系。为后续的应收/应付账款提供业务基础，也是收支事实的业务来源之一。

## 领域定位

```text
  主数据层
  ┌──────────────────────────────────────────────────────┐
  │  AccountSubject ✅  Counterparty ✅  Project ✅        │
  │                         ↑ 被引用                      │
  │  Contract ◄── 本模块    │                              │
  │  - 必须关联 Counterparty（签约方）                     │
  │  - 可选关联 Project（所属项目）                         │
  └──────────────────────────┬─────────────────────────────┘
                             │ 被引用（后续）
                             ▼
  事实层：FactEvent ✅ / Receivable / Payable
```

---

## UML 类图

```text
┌──────────────────────────────────────────────────────────────────┐
│                      <<Aggregate Root>>                           │
│                         Contract                                  │
├──────────────────────────────────────────────────────────────────┤
│ - id: Long                                                        │
│ - code: String              «合同编号，唯一»                      │
│ - name: String              «合同名称»                            │
│ - counterpartyId: Long      «签约往来方»                          │
│ - projectId: Long           «关联项目，可选»                      │
│ - type: ContractType        «值对象: sales/purchase/service»      │
│ - amount: BigDecimal        «合同金额»                            │
│ - signDate: LocalDate       «签约日期»                            │
│ - startDate: LocalDate      «生效日期»                            │
│ - endDate: LocalDate        «到期日期»                            │
│ - status: ContractStatus    «值对象: draft/active/completed/      │
│                               terminated»                         │
│ - remark: String                                                  │
│ - createdAt: LocalDateTime                                        │
│ - updatedAt: LocalDateTime                                        │
├──────────────────────────────────────────────────────────────────┤
│ + create(...): Contract                                           │
│ + reconstruct(...): Contract                                      │
│ + update(...): void                                               │
│ + activate(): void          «draft → active»                      │
│ + complete(): void          «active → completed»                  │
│ + terminate(): void         «draft/active → terminated»           │
└──────────────────────────────────────────────────────────────────┘

┌─────────────────────┐  ┌──────────────────────┐
│  <<Value Object>>   │  │  <<Value Object>>    │
│   ContractType      │  │   ContractStatus     │
├─────────────────────┤  ├──────────────────────┤
│ SALES    (销售合同)  │  │ DRAFT      (草稿)   │
│ PURCHASE (采购合同)  │  │ ACTIVE     (生效)   │
│ SERVICE  (服务合同)  │  │ COMPLETED  (已完成) │
└─────────────────────┘  │ TERMINATED (已终止) │
                         └──────────────────────┘
```

---

## 状态机

```text
┌───────┐  activate()  ┌────────┐  complete()  ┌───────────┐
│ DRAFT ├─────────────►│ ACTIVE ├─────────────►│ COMPLETED │
└───┬───┘              └───┬────┘              └───────────┘
    │                      │
    │    terminate()       │  terminate()
    └──────────┬───────────┘
               ▼
         ┌────────────┐
         │ TERMINATED │
         └────────────┘
```

---

## 领域规则清单

| # | 规则 | 执行层 | 违反时异常 |
|---|------|--------|-----------|
| R1 | `code` 全局唯一 | DomainService | `BusinessException(40002, "合同编号已存在")` |
| R2 | `counterpartyId` 必须关联已有往来方 | DomainService | `BusinessException(40401, "往来方不存在")` |
| R3 | `projectId` 若非空则必须关联已有项目 | DomainService | `BusinessException(40401, "项目不存在")` |
| R4 | `amount` 必须 > 0 | 入参 @Valid | `MethodArgumentNotValidException` |
| R5 | `endDate` 必须 ≥ `startDate` | DomainService | `BusinessException(40003, "到期日期不能早于生效日期")` |
| R6 | 状态流转必须符合状态机 | Domain 聚合根 | `BusinessException(40004, "当前状态不允许此操作")` |
| R7 | 已完成/终止的合同不可编辑 | AppService | `BusinessException(40006, "当前状态不允许编辑")` |
| R8 | 已完成/终止的合同不可删除 | AppService | `BusinessException(40005, "当前状态不允许删除")` |

---

## 数据库映射

```sql
CREATE TABLE `contract` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT,
  `code`             VARCHAR(50)   NOT NULL COMMENT '合同编号',
  `name`             VARCHAR(200)  NOT NULL COMMENT '合同名称',
  `counterparty_id`  BIGINT        NOT NULL COMMENT '签约往来方',
  `project_id`       BIGINT        DEFAULT NULL COMMENT '关联项目',
  `type`             VARCHAR(20)   NOT NULL COMMENT '类型: sales/purchase/service',
  `amount`           DECIMAL(15,2) NOT NULL COMMENT '合同金额',
  `sign_date`        DATE          DEFAULT NULL COMMENT '签约日期',
  `start_date`       DATE          DEFAULT NULL COMMENT '生效日期',
  `end_date`         DATE          DEFAULT NULL COMMENT '到期日期',
  `status`           VARCHAR(20)   NOT NULL DEFAULT 'draft' COMMENT '状态: draft/active/completed/terminated',
  `remark`           VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_counterparty` (`counterparty_id`),
  KEY `idx_project` (`project_id`),
  KEY `idx_status` (`status`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合同';
```

---

## 编码顺序

| 步骤 | 产出 |
|------|------|
| 1 | Flyway `V8__create_contract.sql` + `V9__seed_contract.sql` |
| 2 | Domain 值对象 + 聚合根 + Repository + DomainService |
| 3 | Infrastructure 4 件套 |
| 4 | DTO + VO + AppService |
| 5 | Controller |
| 6 | 前端 API + View + 路由 + 菜单 |
| 7 | 测试 |
