# project 领域设计

## 目标

项目（Project）是组织域的核心主数据，为收支事实提供"归属项目"维度，使系统能够做项目级盈亏分析和预算执行监控。

## 领域定位

```text
  主数据层（已落地）
  ┌──────────────────────────────────────────────┐
  │  AccountSubject ✅   Counterparty ✅           │
  └──────────────────────┬───────────────────────┘
                         │
  组织域 ◄── 本模块       │
  ┌──────────────────────┤───────────────────────┐
  │  Project             │                        │
  │  - 被 FactEvent 引用（后续增加 project_id）   │
  │  - 被 Budget 引用（后续预算编制）             │
  └──────────────────────┬───────────────────────┘
                         │
  事实层（已落地）         │
  ┌──────────────────────┤───────────────────────┐
  │  FactEvent ✅                                  │
  └──────────────────────────────────────────────┘
```

---

## UML 类图

```text
┌────────────────────────────────────────────────────────────────┐
│                     <<Aggregate Root>>                          │
│                        Project                                  │
├────────────────────────────────────────────────────────────────┤
│ - id: Long                                                      │
│ - code: String              «项目编号，唯一»                    │
│ - name: String              «项目名称»                          │
│ - status: ProjectStatus     «值对象: planning/executing/        │
│                               completed/suspended»              │
│ - budget: BigDecimal        «总预算金额»                        │
│ - startDate: LocalDate      «计划开始日期»                      │
│ - endDate: LocalDate        «计划结束日期»                      │
│ - manager: String           «项目经理»                          │
│ - description: String       «项目描述»                          │
│ - enabled: Boolean          «启用状态»                          │
│ - createdAt: LocalDateTime                                      │
│ - updatedAt: LocalDateTime                                      │
├────────────────────────────────────────────────────────────────┤
│ + create(...): Project                                          │
│ + reconstruct(...): Project                                     │
│ + update(...): void                                             │
│ + start(): void              «planning → executing»             │
│ + complete(): void           «executing → completed»            │
│ + suspend(): void            «executing → suspended»            │
│ + resume(): void             «suspended → executing»            │
│ + enable(): void                                                │
│ + disable(): void                                               │
│ + isActive(): boolean        «executing 且 enabled»             │
└────────────────────────────────────────────────────────────────┘

┌────────────────────────┐
│   <<Value Object>>     │
│    ProjectStatus       │
├────────────────────────┤
│ PLANNING   (规划中)    │
│ EXECUTING  (进行中)    │
│ COMPLETED  (已完成)    │
│ SUSPENDED  (已暂停)    │
└────────────────────────┘

┌────────────────────────────────────────────────────────────────┐
│                    <<Repository Interface>>                      │
│                    ProjectRepository                             │
├────────────────────────────────────────────────────────────────┤
│ + findById(id): Optional<Project>                                │
│ + findByCode(code): Optional<Project>                            │
│ + search(keyword, status): List<Project>                         │
│ + save(project): Project                                         │
│ + deleteById(id): void                                           │
│ + existsByCode(code): boolean                                    │
│ + existsByCodeExcludeId(code, id): boolean                       │
└────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────┐
│                   <<Domain Service>>                             │
│                 ProjectDomainService                             │
├────────────────────────────────────────────────────────────────┤
│ + validateCodeUnique(code): void                                 │
│ + validateCodeUniqueForUpdate(code, id): void                    │
│ + validateDateRange(start, end): void                            │
└────────────────────────────────────────────────────────────────┘
```

---

## 状态机

```text
┌──────────┐    start()    ┌──────────┐   complete()  ┌───────────┐
│ PLANNING ├──────────────►│EXECUTING ├──────────────►│ COMPLETED │
└──────────┘               └────┬─────┘               └───────────┘
                                │
                     suspend()  │  resume()
                                ▼
                          ┌──────────┐
                          │SUSPENDED │
                          └──────────┘
```

- `PLANNING → EXECUTING`：调用 start()
- `EXECUTING → COMPLETED`：调用 complete()
- `EXECUTING → SUSPENDED`：调用 suspend()
- `SUSPENDED → EXECUTING`：调用 resume()
- 其他转换非法，抛 BusinessException

---

## 领域规则清单

| # | 规则 | 执行层 | 违反时异常 |
|---|------|--------|-----------|
| R1 | `code` 全局唯一 | DomainService | `BusinessException(40002, "项目编号已存在")` |
| R2 | `endDate` 必须晚于或等于 `startDate` | DomainService | `BusinessException(40003, "结束日期不能早于开始日期")` |
| R3 | `budget` 必须 ≥ 0 | 入参 @Valid | `MethodArgumentNotValidException` |
| R4 | 状态转换需符合状态机 | Domain 聚合根 | `BusinessException(40004, "当前状态不允许此操作")` |
| R5 | 已完成的项目不可删除 | AppService | `BusinessException(40005, "已完成的项目不能删除")` |

---

## 数据库映射

### 建表 DDL

```sql
CREATE TABLE `project` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT,
  `code`         VARCHAR(50)   NOT NULL COMMENT '项目编号',
  `name`         VARCHAR(100)  NOT NULL COMMENT '项目名称',
  `status`       VARCHAR(20)   NOT NULL DEFAULT 'planning' COMMENT '状态: planning/executing/completed/suspended',
  `budget`       DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '总预算',
  `start_date`   DATE          DEFAULT NULL COMMENT '计划开始日期',
  `end_date`     DATE          DEFAULT NULL COMMENT '计划结束日期',
  `manager`      VARCHAR(50)   DEFAULT NULL COMMENT '项目经理',
  `description`  VARCHAR(500)  DEFAULT NULL COMMENT '项目描述',
  `enabled`      TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '启用状态',
  `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_status` (`status`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目';
```

### 种子数据

```sql
INSERT INTO `project` (`code`, `name`, `status`, `budget`, `start_date`, `end_date`, `manager`, `description`) VALUES
('PRJ-2026-001', '项目X', 'executing', 200000.00, '2026-05-01', '2026-08-31', '张三', '示例项目：核心产品线'),
('PRJ-2026-002', '项目Y', 'planning', 100000.00, '2026-06-01', '2026-09-30', '李四', '示例项目：新市场拓展');
```

---

## 编码顺序

| 步骤 | 产出 |
|------|------|
| 1 | Flyway 迁移脚本 `V6__create_project.sql` + `V7__seed_project.sql` |
| 2 | Domain 值对象 `ProjectStatus` + 聚合根 `Project` |
| 3 | Repository 接口 + DomainService |
| 4 | Infrastructure Mapper + Entity + Converter + RepositoryImpl |
| 5 | DTO + VO + Application Service |
| 6 | Controller |
| 7 | 前端 API + 页面 |
| 8 | 测试 |
