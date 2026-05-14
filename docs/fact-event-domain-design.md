# fact_event 领域设计

## 目标

收支事实（FactEvent）是系统的唯一数据写入口，记录企业经营中"发生了什么"。所有后续的归因、指标、决策都以事实为基础。

## 领域定位

```text
  主数据层（已落地）
  ┌─────────────────────────────────────────────┐
  │  AccountSubject ✅    Counterparty ✅         │
  └──────────────────────┬──────────────────────┘
                         │ 被引用
                         ▼
  事实层 ◄── 本模块
  ┌─────────────────────────────────────────────┐
  │  FactEvent（每笔收支事实）                     │
  │  - 必须关联 AccountSubject（科目）             │
  │  - 必须关联 Counterparty（往来方）             │
  └──────────────────────┬──────────────────────┘
                         │ 被消费（后续迭代）
                         ▼
  归因层：Attribution
  指标层：MetricSnapshot
```

---

## UML 类图

```text
┌──────────────────────────────────────────────────────────────┐
│                    <<Aggregate Root>>                         │
│                       FactEvent                              │
├──────────────────────────────────────────────────────────────┤
│ - id: Long                                                   │
│ - type: FactType              «值对象: income/cost»          │
│ - amount: BigDecimal          «精度 15,2»                    │
│ - businessDate: LocalDate     «业务发生日期»                  │
│ - accountingDate: LocalDate   «会计确认日期»                  │
│ - subjectId: Long             «关联会计科目»                  │
│ - counterpartyId: Long        «关联往来方»                    │
│ - costCategory: CostCategory  «值对象: fixed/variable/direct/indirect，仅成本»│
│ - invoiceNo: String           «发票号，可选»                  │
│ - status: FactStatus          «值对象: valid/reversed»       │
│ - remark: String                                             │
│ - createdAt: LocalDateTime                                   │
│ - updatedAt: LocalDateTime                                   │
├──────────────────────────────────────────────────────────────┤
│ + create(...): FactEvent                                     │
│ + reverse(): void             «冲正，不可物理删除»            │
│ + isReversed(): boolean                                      │
└──────────────────────────────────────────────────────────────┘

┌─────────────────────┐  ┌─────────────────────┐  ┌─────────────────────┐
│  <<Value Object>>   │  │  <<Value Object>>   │  │  <<Value Object>>   │
│     FactType        │  │    CostCategory     │  │     FactStatus      │
├─────────────────────┤  ├─────────────────────┤  ├─────────────────────┤
│ INCOME              │  │ FIXED               │  │ VALID               │
│ COST                │  │ VARIABLE            │  │ REVERSED            │
└─────────────────────┘  │ DIRECT              │  └─────────────────────┘
                         │ INDIRECT            │
                         └─────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                   <<Repository Interface>>                     │
│                   FactEventRepository                         │
├──────────────────────────────────────────────────────────────┤
│ + findById(id): Optional<FactEvent>                           │
│ + search(keyword, type, status, startDate, endDate):          │
│       List<FactEvent>                                         │
│ + save(event): FactEvent                                      │
│ + countBySubjectId(subjectId): long                           │
│ + countByCounterpartyId(counterpartyId): long                 │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                  <<Domain Service>>                            │
│                FactEventDomainService                         │
├──────────────────────────────────────────────────────────────┤
│ + validateSubjectExists(subjectId): void                      │
│ + validateCounterpartyExists(counterpartyId): void            │
│ + validateNotAlreadyReversed(event): void                     │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                  <<Application Service>>                       │
│                 FactEventAppService                            │
├──────────────────────────────────────────────────────────────┤
│ + @Transactional create(dto): FactEventVO                     │
│ + @Transactional reverse(id): void                            │
│ + list(query): List<FactEventVO>                              │
│ + getDetail(id): FactEventVO                                  │
└──────────────────────────────────────────────────────────────┘
```

---

## 领域规则清单

| # | 规则 | 执行层 | 违反时异常 |
|---|------|--------|-----------|
| R1 | `subjectId` 必须关联已存在的会计科目 | DomainService | `BusinessException(40401, "会计科目不存在")` |
| R2 | `counterpartyId` 必须关联已存在的往来方 | DomainService | `BusinessException(40401, "往来方不存在")` |
| R3 | `amount` 必须大于 0 | 入参 @Valid | `MethodArgumentNotValidException` |
| R4 | `costCategory` 仅在 type=cost 时有效 | Domain.create() | 自动处理：income 时置为 null |
| R5 | 事实不可物理删除，只能冲正（reversed） | AppService | 不暴露 delete 接口 |
| R6 | 已冲正的事实不能再次冲正 | DomainService | `BusinessException(40001, "该记录已冲正")` |
| R7 | `status` 默认 VALID | Domain.create() | 自动设置 |
| R8 | `accountingDate` 默认等于 `businessDate` | Domain.create() | 前端可修改 |

---

## 数据库映射

### 建表 DDL

```sql
CREATE TABLE `fact_event` (
  `id`               BIGINT         NOT NULL AUTO_INCREMENT,
  `type`             VARCHAR(20)    NOT NULL COMMENT '类型: income/cost',
  `amount`           DECIMAL(15,2)  NOT NULL COMMENT '金额',
  `business_date`    DATE           NOT NULL COMMENT '业务发生日期',
  `accounting_date`  DATE           NOT NULL COMMENT '会计确认日期',
  `subject_id`       BIGINT         NOT NULL COMMENT '会计科目ID',
  `counterparty_id`  BIGINT         NOT NULL COMMENT '往来方ID',
  `cost_category`    VARCHAR(20)    DEFAULT NULL COMMENT '成本类别: fixed/variable/direct/indirect',
  `invoice_no`       VARCHAR(50)    DEFAULT NULL COMMENT '发票号',
  `status`           VARCHAR(20)    NOT NULL DEFAULT 'valid' COMMENT '状态: valid/reversed',
  `remark`           VARCHAR(500)   DEFAULT NULL COMMENT '备注',
  `created_at`       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_type_status` (`type`, `status`),
  KEY `idx_accounting_date` (`accounting_date`),
  KEY `idx_subject` (`subject_id`),
  KEY `idx_counterparty` (`counterparty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收支事实';
```

---

## 编码顺序

| 步骤 | 产出 |
|------|------|
| 1 | Flyway 迁移脚本 `V5__create_fact_event.sql` |
| 2 | Domain 实体 + 值对象 + Repository 接口 |
| 3 | Infrastructure Mapper + Entity + Repository 实现 |
| 4 | DomainService（校验规则） |
| 5 | Application Service + DTO + VO |
| 6 | Controller |
| 7 | 前端 API + 页面 |
| 8 | 测试 |
