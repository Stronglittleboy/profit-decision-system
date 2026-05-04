# 飞牛经营系统 - 数据库设计文档

## 📊 数据库概览

**数据库名：** jeecg-boot  
**字符集：** utf8mb4  
**排序规则：** utf8mb4_unicode_ci  
**引擎：** InnoDB  

**表统计：**
- jeecg-boot 系统表：100+ 张
- profit 业务表：9 张
- **总计：110+ 张**

---

## 🏗️ profit 业务表结构

### 1. fact_event（收支记录表）

**用途：** 记录所有收入和成本事实

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| business_date | DATE | 业务发生日期 |
| accounting_date | DATE | 会计确认日期 |
| cash_date | DATE | 现金流日期 |
| type | VARCHAR(20) | 类型：income/cost |
| amount | DECIMAL(15,2) | 金额 |
| cost_category | VARCHAR(20) | 成本类别：fixed/variable/direct/indirect |
| counterparty_id | BIGINT | 客户/供应商ID |
| project_id | BIGINT | 项目ID |
| org_unit_id | BIGINT | 组织单元ID |
| invoice_no | VARCHAR(50) | 发票号 |
| status | VARCHAR(20) | 状态：valid/reversed |
| created_at | DATETIME | 创建时间 |

**索引：**
- PRIMARY KEY (id)
- INDEX idx_accounting_date (accounting_date)
- INDEX idx_type_status (type, status)

---

### 2. receivable（应收账款表）

**用途：** 管理应收账款

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| fact_id | BIGINT | 关联收入事实ID |
| counterparty_id | BIGINT | 客户ID |
| total_amount | DECIMAL(15,2) | 总金额 |
| received_amount | DECIMAL(15,2) | 已收金额 |
| outstanding_amount | DECIMAL(15,2) | 未收金额 |
| due_date | DATE | 到期日 |
| status | VARCHAR(20) | 状态：outstanding/overdue/settled |
| created_at | DATETIME | 创建时间 |

**索引：**
- PRIMARY KEY (id)
- INDEX idx_fact (fact_id)
- INDEX idx_counterparty (counterparty_id)
- INDEX idx_status (status)

---

### 3. payable（应付账款表）

**用途：** 管理应付账款

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| fact_id | BIGINT | 关联成本事实ID |
| counterparty_id | BIGINT | 供应商ID |
| total_amount | DECIMAL(15,2) | 总金额 |
| paid_amount | DECIMAL(15,2) | 已付金额 |
| outstanding_amount | DECIMAL(15,2) | 未付金额 |
| due_date | DATE | 到期日 |
| status | VARCHAR(20) | 状态：outstanding/overdue/settled |
| created_at | DATETIME | 创建时间 |

**索引：**
- PRIMARY KEY (id)
- INDEX idx_fact (fact_id)
- INDEX idx_counterparty (counterparty_id)

---

### 4. period_closing（期间结账表）

**用途：** 管理会计期间结账

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| period | VARCHAR(20) | 期间：2026-05 |
| org_unit_id | BIGINT | 组织单元ID |
| status | VARCHAR(20) | 状态：open/closing/closed |
| closed_by | BIGINT | 结账人ID |
| closed_at | DATETIME | 结账时间 |
| created_at | DATETIME | 创建时间 |

**索引：**
- PRIMARY KEY (id)
- UNIQUE KEY uk_period_org (period, org_unit_id)

---

### 5. counterparty（客户/供应商表）

**用途：** 管理客户和供应商信息

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(100) | 名称 |
| type | VARCHAR(20) | 类型：customer/supplier |
| contact | VARCHAR(100) | 联系人 |
| phone | VARCHAR(20) | 电话 |
| address | VARCHAR(200) | 地址 |
| tax_no | VARCHAR(50) | 税号 |
| credit_level | VARCHAR(20) | 信用等级：A/B/C/D |
| status | VARCHAR(20) | 状态：active/inactive |
| created_at | DATETIME | 创建时间 |

**索引：**
- PRIMARY KEY (id)
- INDEX idx_type (type)

---

### 6. contract（合同表）

**用途：** 管理合同信息

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| contract_no | VARCHAR(50) | 合同编号 |
| counterparty_id | BIGINT | 客户/供应商ID |
| total_amount | DECIMAL(15,2) | 合同总额 |
| start_date | DATE | 开始日期 |
| end_date | DATE | 结束日期 |
| status | VARCHAR(20) | 状态：executing/completed/terminated |
| created_at | DATETIME | 创建时间 |

**索引：**
- PRIMARY KEY (id)
- UNIQUE KEY uk_contract_no (contract_no)

---

### 7. project（项目表）

**用途：** 管理项目信息

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(100) | 项目名称 |
| code | VARCHAR(50) | 项目编号 |
| org_unit_id | BIGINT | 所属组织 |
| manager_id | BIGINT | 项目经理 |
| budget | DECIMAL(15,2) | 预算 |
| start_date | DATE | 开始日期 |
| end_date | DATE | 结束日期 |
| status | VARCHAR(20) | 状态：planning/executing/closed |
| created_at | DATETIME | 创建时间 |

**索引：**
- PRIMARY KEY (id)
- UNIQUE KEY uk_code (code)

---

### 8. budget（预算表）

**用途：** 管理预算

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| org_unit_id | BIGINT | 组织单元ID |
| period | VARCHAR(20) | 期间：2026-05 |
| category | VARCHAR(50) | 类别：revenue/fixed_cost等 |
| budgeted_amount | DECIMAL(15,2) | 预算金额 |
| approved_by | BIGINT | 审批人ID |
| status | VARCHAR(20) | 状态：draft/approved |
| created_at | DATETIME | 创建时间 |

**索引：**
- PRIMARY KEY (id)
- UNIQUE KEY uk_org_period_category (org_unit_id, period, category)

---

### 9. budget_adjustment（预算调整表）

**用途：** 管理预算调整

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| budget_id | BIGINT | 预算ID |
| old_amount | DECIMAL(15,2) | 原金额 |
| new_amount | DECIMAL(15,2) | 新金额 |
| reason | TEXT | 调整原因 |
| requested_by | BIGINT | 申请人ID |
| approved_by | BIGINT | 审批人ID |
| status | VARCHAR(20) | 状态：pending/approved/rejected |
| created_at | DATETIME | 创建时间 |

**索引：**
- PRIMARY KEY (id)
- INDEX idx_budget (budget_id)

---

## 🔗 表关系图

```
fact_event (收支记录)
    ├─→ receivable (应收账款)
    ├─→ payable (应付账款)
    ├─→ counterparty (客户/供应商)
    ├─→ project (项目)
    └─→ org_unit (组织)

period_closing (期间结账)
    └─→ org_unit (组织)

contract (合同)
    └─→ counterparty (客户/供应商)

project (项目)
    └─→ org_unit (组织)

budget (预算)
    ├─→ org_unit (组织)
    └─→ budget_adjustment (预算调整)
```

---

## 📊 数据流向

```
1. 收入流程：
   fact_event (type=income) 
   → receivable (应收账款)
   → 收款后更新 received_amount

2. 成本流程：
   fact_event (type=cost)
   → payable (应付账款)
   → 付款后更新 paid_amount

3. 结账流程：
   period_closing (status=open)
   → 检查所有 fact_event
   → status=closed（锁定期间）
```

---

## 🎯 设计原则

1. **单一事实源**：fact_event 是唯一的数据写入口
2. **可追溯**：所有记录保留 created_at
3. **软删除**：使用 status 标记，不物理删除
4. **索引优化**：常用查询字段建立索引
5. **数据完整性**：外键关系通过应用层保证

---

**文档版本：** v1.0  
**创建时间：** 2026-05-08  
**状态：** ✅ 已导入
