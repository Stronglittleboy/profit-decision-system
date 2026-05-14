# 飞牛经营系统 - 数据库设计文档

## 数据库概览

**数据库名：** profit_system
**字符集：** utf8mb4
**排序规则：** utf8mb4_unicode_ci
**引擎：** InnoDB
**迁移工具：** Flyway（Spring Boot 启动时自动执行）

**迁移脚本位置：** `backend/src/main/resources/db/migration/`

---

## 已落地表

### 1. account_subject（会计科目表）

**迁移脚本：** `V1__create_account_subject.sql` + `V2__seed_account_subject.sql`
**用途：** 树形会计科目主数据，支持多级父子关系

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO_INCREMENT | 主键 |
| code | VARCHAR(50) NOT NULL | 科目编码（唯一） |
| name | VARCHAR(100) NOT NULL | 科目名称 |
| parent_id | BIGINT | 父科目ID（NULL=根科目） |
| level | INT DEFAULT 1 | 科目层级（由 parentId 推导） |
| type | VARCHAR(20) NOT NULL | 科目类型: asset/liability/equity/cost/profit_loss |
| debit_credit | VARCHAR(20) NOT NULL | 借贷方向: debit/credit |
| enabled | TINYINT(1) DEFAULT 1 | 启用状态 |
| sort | INT DEFAULT 0 | 排序 |
| remark | VARCHAR(200) | 备注 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间（自动） |

**索引：** PRIMARY(id), UNIQUE(code), INDEX(parent_id), INDEX(type)

**种子数据：** 8 条一级科目（库存现金、银行存款、应收账款、应付账款、主营业务收入、主营业务成本、管理费用、销售费用）

---

### 2. counterparty（往来方表）

**迁移脚本：** `V3__create_counterparty.sql` + `V4__seed_counterparty.sql`
**用途：** 客户/供应商主数据

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO_INCREMENT | 主键 |
| name | VARCHAR(100) NOT NULL | 名称 |
| type | VARCHAR(20) NOT NULL | 类型: customer/supplier/both |
| contact | VARCHAR(100) | 联系人 |
| phone | VARCHAR(20) | 电话 |
| address | VARCHAR(200) | 地址 |
| tax_no | VARCHAR(50) | 税号 |
| credit_level | VARCHAR(10) | 信用等级: A/B/C/D |
| enabled | TINYINT(1) DEFAULT 1 | 启用状态 |
| remark | VARCHAR(200) | 备注 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间（自动） |

**索引：** PRIMARY(id), INDEX(type), INDEX(name)

**种子数据：** 4 条（示例客户A/B、示例供应商X/Y）

---

### 3. fact_event（收支事实表）

**迁移脚本：** `V5__create_fact_event.sql`
**用途：** 收支事实唯一入口，记录企业的每笔收入和成本

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO_INCREMENT | 主键 |
| type | VARCHAR(20) NOT NULL | 类型: income/cost |
| amount | DECIMAL(15,2) NOT NULL | 金额 |
| business_date | DATE NOT NULL | 业务发生日期 |
| accounting_date | DATE NOT NULL | 会计确认日期 |
| subject_id | BIGINT NOT NULL | 关联会计科目 |
| counterparty_id | BIGINT NOT NULL | 关联往来方 |
| cost_category | VARCHAR(20) | 成本类别: fixed/variable/direct/indirect |
| invoice_no | VARCHAR(50) | 发票号 |
| status | VARCHAR(20) NOT NULL DEFAULT 'valid' | 状态: valid/reversed |
| remark | VARCHAR(500) | 备注 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间（自动） |

**索引：** PRIMARY(id), INDEX(type, status), INDEX(accounting_date), INDEX(subject_id), INDEX(counterparty_id)

**约束：** 不可物理删除，只能冲正（status → reversed）

---

### 4. project（项目表）

**迁移脚本：** `V6__create_project.sql` + `V7__seed_project.sql`
**用途：** 项目主数据，支持项目全生命周期管理和项目级盈亏分析

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO_INCREMENT | 主键 |
| code | VARCHAR(50) NOT NULL | 项目编号（唯一） |
| name | VARCHAR(100) NOT NULL | 项目名称 |
| status | VARCHAR(20) NOT NULL DEFAULT 'planning' | 状态: planning/executing/completed/suspended |
| budget | DECIMAL(15,2) NOT NULL DEFAULT 0 | 总预算 |
| start_date | DATE | 计划开始日期 |
| end_date | DATE | 计划结束日期 |
| manager | VARCHAR(50) | 项目经理 |
| description | VARCHAR(500) | 项目描述 |
| enabled | TINYINT(1) DEFAULT 1 | 启用状态 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间（自动） |

**索引：** PRIMARY(id), UNIQUE(code), INDEX(status), INDEX(name)

**种子数据：** 2 条（示例项目X/Y）

**状态机：** planning → executing → completed, executing ↔ suspended

---

### 5. contract（合同表）

**迁移脚本：** `V8__create_contract.sql` + `V9__seed_contract.sql`
**用途：** 合同主数据，记录与往来方的契约关系

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO_INCREMENT | 主键 |
| code | VARCHAR(50) NOT NULL | 合同编号（唯一） |
| name | VARCHAR(200) NOT NULL | 合同名称 |
| counterparty_id | BIGINT NOT NULL | 签约往来方 |
| project_id | BIGINT | 关联项目（可选） |
| type | VARCHAR(20) NOT NULL | 类型: sales/purchase/service |
| amount | DECIMAL(15,2) NOT NULL | 合同金额 |
| sign_date | DATE | 签约日期 |
| start_date | DATE | 生效日期 |
| end_date | DATE | 到期日期 |
| status | VARCHAR(20) NOT NULL DEFAULT 'draft' | 状态: draft/active/completed/terminated |
| remark | VARCHAR(500) | 备注 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间（自动） |

**索引：** PRIMARY(id), UNIQUE(code), INDEX(counterparty_id), INDEX(project_id), INDEX(status), INDEX(type)

**种子数据：** 3 条（销售/采购/服务各一份示例合同）

**状态机：** draft → active → completed, draft/active → terminated

---

### 6. receivable（应收账款表）

**迁移脚本：** `V10__create_receivable.sql` + `V12__seed_receivable.sql`
**用途：** 客户应收账款跟踪，登记回款、标记逾期

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO_INCREMENT | 主键 |
| code | VARCHAR(50) NOT NULL | 单据编号（唯一） |
| counterparty_id | BIGINT NOT NULL | 客户 |
| contract_id | BIGINT | 关联合同（可选） |
| amount | DECIMAL(15,2) NOT NULL | 应收总额 |
| paid_amount | DECIMAL(15,2) NOT NULL DEFAULT 0 | 已收金额 |
| due_date | DATE NOT NULL | 到期日 |
| status | VARCHAR(20) NOT NULL DEFAULT 'pending' | 状态: pending/partial/paid/overdue |
| remark | VARCHAR(500) | 备注 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间（自动） |

**索引：** PRIMARY(id), UNIQUE(code), INDEX(counterparty_id), INDEX(status), INDEX(due_date)

**状态机：** pending → partial → paid, pending/partial → overdue, overdue → partial → paid

---

### 7. payable（应付账款表）

**迁移脚本：** `V11__create_payable.sql` + `V13__seed_payable.sql`
**用途：** 供应商应付账款跟踪，登记付款、标记逾期

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO_INCREMENT | 主键 |
| code | VARCHAR(50) NOT NULL | 单据编号（唯一） |
| counterparty_id | BIGINT NOT NULL | 供应商 |
| contract_id | BIGINT | 关联合同（可选） |
| amount | DECIMAL(15,2) NOT NULL | 应付总额 |
| paid_amount | DECIMAL(15,2) NOT NULL DEFAULT 0 | 已付金额 |
| due_date | DATE NOT NULL | 到期日 |
| status | VARCHAR(20) NOT NULL DEFAULT 'pending' | 状态: pending/partial/paid/overdue |
| remark | VARCHAR(500) | 备注 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间（自动） |

**索引：** PRIMARY(id), UNIQUE(code), INDEX(counterparty_id), INDEX(status), INDEX(due_date)

**状态机：** pending → partial → paid, pending/partial → overdue, overdue → partial → paid

---

## 规划中的表（按业务域排列）

以下表在需求文档中已定义，将在后续迭代中按优先级落地：

| 表名 | 业务域 | 说明 | 优先级 |
|------|--------|------|--------|
| budget | 预算域 | 预算编制 | P4 |
| budget_adjustment | 预算域 | 预算调整 | P4 |
| period_closing | 结账域 | 期间结账 | P4 |

---

## Flyway 迁移记录

| 版本 | 文件 | 说明 |
|------|------|------|
| V1 | `V1__create_account_subject.sql` | 会计科目表 DDL |
| V2 | `V2__seed_account_subject.sql` | 会计科目种子数据 |
| V3 | `V3__create_counterparty.sql` | 往来方表 DDL |
| V4 | `V4__seed_counterparty.sql` | 往来方种子数据 |
| V5 | `V5__create_fact_event.sql` | 收支事实表 DDL |
| V6 | `V6__create_project.sql` | 项目表 DDL |
| V7 | `V7__seed_project.sql` | 项目种子数据 |
| V8 | `V8__create_contract.sql` | 合同表 DDL |
| V9 | `V9__seed_contract.sql` | 合同种子数据 |
| V10 | `V10__create_receivable.sql` | 应收账款表 DDL |
| V11 | `V11__create_payable.sql` | 应付账款表 DDL |
| V12 | `V12__seed_receivable.sql` | 应收账款种子数据 |
| V13 | `V13__seed_payable.sql` | 应付账款种子数据 |

---

## 设计原则

1. **Flyway 管理迁移**：所有 DDL/DML 变更通过 `db/migration/V*__.sql` 管理，Spring Boot 启动自动执行
2. **可追溯**：所有记录保留 created_at / updated_at
3. **软删除预留**：当前使用物理删除，后续可按需切换 enabled 标记
4. **索引优化**：常用查询字段建立索引
5. **数据完整性**：外键关系通过应用层保证（不使用数据库外键约束）
