# counterparty 领域设计

## 目标

往来方（客户/供应商）是收支事实的交易对手，属于主数据域。所有 FactEvent 在录入时都必须关联到具体的往来方。往来方管理在科目之后落地，复用同一分层模式。

## 领域定位

```text
系统全景中的位置：

  主数据层
  ┌─────────────────────────────────────────────┐
  │  AccountSubject   ✅ 已落地                   │
  │  Counterparty     ◄── 本模块                  │
  │  OrgUnit / Project / Contract / Budget       │
  └──────────────────────┬──────────────────────┘
                         │ 被引用
                         ▼
  事实层：FactEvent（每笔收支必须挂往来方）
  应收应付：Receivable / Payable（关联往来方）
  合同层：Contract（关联往来方）
```

`counterparty` 只承担主数据的 CRUD + 列表管理，不参与事实计算。

---

## UML 类图

```text
┌──────────────────────────────────────────────────────────────┐
│                    <<Aggregate Root>>                         │
│                      Counterparty                            │
├──────────────────────────────────────────────────────────────┤
│ - id: Long                                                   │
│ - name: String              «必填»                           │
│ - type: CounterpartyType    «值对象: customer/supplier/both» │
│ - contact: String           «联系人»                         │
│ - phone: String             «电话»                           │
│ - address: String           «地址»                           │
│ - taxNo: String             «税号»                           │
│ - creditLevel: CreditLevel  «值对象: A/B/C/D»               │
│ - enabled: Boolean          «默认 true»                      │
│ - remark: String                                             │
│ - createdAt: LocalDateTime                                   │
│ - updatedAt: LocalDateTime                                   │
├──────────────────────────────────────────────────────────────┤
│ + create(...): Counterparty                                  │
│ + update(...): void                                          │
│ + enable(): void                                             │
│ + disable(): void                                            │
└──────────────────────────────────────────────────────────────┘
         │ type                          │ creditLevel
         ▼                              ▼
┌─────────────────────┐    ┌──────────────────────┐
│  <<Value Object>>   │    │   <<Value Object>>   │
│  CounterpartyType   │    │     CreditLevel      │
├─────────────────────┤    ├──────────────────────┤
│ CUSTOMER            │    │ A                    │
│ SUPPLIER            │    │ B                    │
│ BOTH                │    │ C                    │
├─────────────────────┤    │ D                    │
│ + fromCode(String)  │    └──────────────────────┘
│ + toCode(): String  │
└─────────────────────┘


┌──────────────────────────────────────────────────────────────┐
│                   <<Repository Interface>>                     │
│                  CounterpartyRepository                        │
├──────────────────────────────────────────────────────────────┤
│ + findById(id: Long): Optional<Counterparty>                  │
│ + findAll(): List<Counterparty>                               │
│ + existsByName(name: String): boolean                         │
│ + existsByNameAndIdNot(name: String, id: Long): boolean       │
│ + save(counterparty: Counterparty): Counterparty              │
│ + deleteById(id: Long): void                                  │
│ + search(keyword: String): List<Counterparty>                 │
│ + findByType(type: String): List<Counterparty>                │
└──────────────────────────────────────────────────────────────┘


┌──────────────────────────────────────────────────────────────┐
│                  <<Domain Service>>                            │
│              CounterpartyDomainService                         │
├──────────────────────────────────────────────────────────────┤
│ - repository: CounterpartyRepository                          │
├──────────────────────────────────────────────────────────────┤
│ + validateNameUnique(name: String): void                      │
│ + validateNameUniqueForUpdate(name: String, id: Long): void   │
└──────────────────────────────────────────────────────────────┘


┌──────────────────────────────────────────────────────────────┐
│                  <<Application Service>>                       │
│               CounterpartyAppService                          │
├──────────────────────────────────────────────────────────────┤
│ + @Transactional create(dto): CounterpartyVO                  │
│ + @Transactional update(id, dto): CounterpartyVO              │
│ + @Transactional delete(id): void                             │
│ + @Transactional toggleStatus(id, enabled): void              │
│ + list(keyword, type): List<CounterpartyVO>                   │
│ + getDetail(id): CounterpartyVO                               │
└──────────────────────────────────────────────────────────────┘
```

---

## 领域规则清单

| # | 规则 | 执行层 | 违反时异常 |
|---|------|--------|-----------|
| R1 | `name` 同类型下唯一 | DomainService | `BusinessException(40901, "往来方名称已存在")` |
| R2 | `type` 必须是枚举内合法值 | 入参 @Valid + 值对象构造 | `MethodArgumentNotValidException` |
| R3 | `creditLevel` 为可选，但若填写必须是合法枚举值 | 值对象构造 | `BusinessException` |
| R4 | `enabled` 默认 true | Domain.create() | 自动设置 |

---

## 数据库映射

### 建表 DDL

```sql
CREATE TABLE `counterparty` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT,
  `name`         VARCHAR(100)  NOT NULL COMMENT '名称',
  `type`         VARCHAR(20)   NOT NULL COMMENT '类型: customer/supplier/both',
  `contact`      VARCHAR(100)  DEFAULT NULL COMMENT '联系人',
  `phone`        VARCHAR(20)   DEFAULT NULL COMMENT '电话',
  `address`      VARCHAR(200)  DEFAULT NULL COMMENT '地址',
  `tax_no`       VARCHAR(50)   DEFAULT NULL COMMENT '税号',
  `credit_level` VARCHAR(10)   DEFAULT NULL COMMENT '信用等级: A/B/C/D',
  `enabled`      TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '启用状态: 1=启用, 0=停用',
  `remark`       VARCHAR(200)  DEFAULT NULL COMMENT '备注',
  `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='往来方（客户/供应商）';
```

### 种子数据

```sql
INSERT INTO `counterparty` (`name`, `type`, `contact`, `phone`, `credit_level`, `enabled`) VALUES
  ('示例客户A', 'customer', '张三', '13800000001', 'A', 1),
  ('示例客户B', 'customer', '李四', '13800000002', 'B', 1),
  ('示例供应商X', 'supplier', '王五', '13800000003', 'A', 1),
  ('示例供应商Y', 'supplier', '赵六', '13800000004', 'B', 1);
```

---

## 编码顺序

| 步骤 | 产出 | 涉及文件 |
|------|------|---------|
| 1 | Flyway 迁移脚本 | `db/migration/V3__create_counterparty.sql` |
| 2 | Domain 实体 + 值对象 + Repository 接口 | `domain/counterparty/*.java` |
| 3 | Infrastructure Mapper + Entity + Repository 实现 | `infrastructure/counterparty/*.java` |
| 4 | DomainService | `domain/counterparty/CounterpartyDomainService.java` |
| 5 | Application Service + DTO + VO | `application/CounterpartyAppService.java` |
| 6 | Controller | `controller/CounterpartyController.java` |
| 7 | 前端 API + 页面 | `frontend/src/api/counterparty.ts` + `views/CounterpartyView.vue` |
| 8 | 测试 | `test/.../CounterpartyDomainServiceTests.java` |
