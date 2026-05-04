# 第四次评审会议纪要

**时间：** 2026-05-01 下午  
**参会人员：** 架构师（25%）、业务人员（25%）、财务（25%）、产品经理（25%）  
**议题：** 终审设计方案

---

## 💰 财务视角（25% 权重）

### ✅ 认可的设计

1. **会计准则符合性**
   - business_date / accounting_date 分离 ✅
   - 支持权责发生制
   - 发票管理完整

2. **成本核算体系**
   - 四类成本分类（固定/变动/直接/间接）✅
   - 跨期分摊机制 ✅
   - 归因规则清晰

3. **审计要求**
   - 审计日志完整 ✅
   - 数据不可删除（冲正机制）✅
   - 版本控制 ✅

### ⚠️ 财务风险（按严重程度排序）

#### 🔴 P0 - 致命问题

**1. 缺少会计科目体系**
```
现状：只有 type（income/cost）和 cost_category
问题：无法生成标准财务报表（资产负债表、现金流量表）

财务需求：
- 收入要区分：主营业务收入、其他业务收入、营业外收入
- 成本要区分：主营业务成本、销售费用、管理费用、财务费用
- 需要对接会计科目表

建议：
fact_event 增加：
- account_code（会计科目代码）
- account_name（会计科目名称）
- subject_type（科目类型：asset/liability/equity/revenue/expense）

新增表：
accounting_subject（会计科目表）
├── code（科目代码：6001/6401）
├── name（科目名称：主营业务收入）
├── type（科目类型：revenue/expense）
├── parent_code（父科目）
└── level（科目级次：1/2/3）
```

**2. 缺少期间结账机制**
```
现状：可以随时录入任意期间的数据
问题：已结账期间还能修改，财务数据不稳定

财务需求：
- 每月结账后，该月数据不可修改
- 结账前要检查：所有单据已审核、归因完成、指标计算完成
- 结账后只能通过"反结账"才能修改

建议：
新增表：
period_closing（期间结账表）
├── period（2026-05）
├── status（open/closing/closed）
├── closed_by（结账人）
├── closed_at（结账时间）
└── checklist（结账检查清单 JSON）

fact_event 增加约束：
- 不允许录入已结账期间的数据
- 修改时检查 period_closing.status
```

**3. 缺少往来账管理**
```
现状：只记录收入/成本，不记录应收/应付
问题：无法管理账期、催款、对账

财务场景：
- 5月10日签合同 50000 元（确认收入）
- 5月20日收款 30000 元（应收减少）
- 6月10日收款 20000 元（应收清零）

建议：
新增表：
receivable（应收账款）
├── counterparty_id
├── fact_id（关联收入）
├── total_amount（总金额）
├── received_amount（已收金额）
├── outstanding_amount（未收金额）
├── due_date（到期日）
└── status（outstanding/overdue/settled）

payable（应付账款）
├── counterparty_id
├── fact_id（关联成本）
├── total_amount
├── paid_amount
├── outstanding_amount
├── due_date
└── status
```

**4. 税务处理不完整**
```
现状：只有 tax_rate 和 tax_amount
问题：无法处理进项税抵扣、增值税申报

财务需求：
- 收入：销项税额
- 成本：进项税额（可抵扣）
- 增值税 = 销项税 - 进项税

建议：
fact_event 增加：
- tax_type（税种：vat/income_tax/other）
- is_deductible（是否可抵扣）
- tax_invoice_type（发票类型：special/normal）

新增表：
tax_declaration（税务申报表）
├── period
├── tax_type
├── output_tax（销项税）
├── input_tax（进项税）
├── payable_tax（应交税额）
└── status（draft/submitted/paid）
```

#### 🟡 P1 - 重要问题

**5. 缺少银行对账**
```
问题：cash_date 记录了现金流，但没有银行账户信息

建议：
fact_event 增加：
- bank_account_id（银行账户）
- transaction_no（银行流水号）

新增表：
bank_account（银行账户）
bank_transaction（银行流水）
```

**6. 缺少固定资产管理**
```
问题：购买设备 10 万，应该按折旧分摊，而不是一次性计入成本

建议：
新增表：
fixed_asset（固定资产）
├── name（资产名称）
├── original_value（原值）
├── depreciation_method（折旧方法：straight_line/declining）
├── useful_life（使用年限）
├── residual_value（残值）
└── accumulated_depreciation（累计折旧）

每月自动生成折旧费用 Fact
```

**7. 缺少成本中心**
```
问题：org_unit 既是组织结构，又是成本归属，混淆了

财务概念：
- 组织结构：公司 → 部门 → 小组（管理层级）
- 成本中心：生产中心、销售中心、管理中心（成本归集）

建议：
新增表：
cost_center（成本中心）
├── code（成本中心代码）
├── name（成本中心名称）
├── type（production/sales/admin）
└── manager_id

fact_event 增加：
- cost_center_id（成本中心）
```

---

## 👔 业务人员补充意见（25% 权重）

### ⚠️ 业务风险

#### 🔴 P0 - 致命问题

**8. 缺少合同管理**
```
现状：reference_id 只是字符串
问题：无法跟踪合同执行情况

业务场景：
- 签订合同 100 万
- 分 5 期交付，每期确认收入 20 万
- 需要跟踪：合同总额、已确认收入、未确认收入

建议：
新增表：
contract（合同）
├── contract_no（合同编号）
├── counterparty_id
├── total_amount（合同总额）
├── start_date / end_date
├── payment_terms（付款条件）
└── status（executing/completed/terminated）

contract_milestone（合同里程碑）
├── contract_id
├── milestone_name（里程碑名称）
├── amount（金额）
├── due_date（计划日期）
├── actual_date（实际日期）
└── status（pending/completed）

fact_event 增加：
- contract_id（关联合同）
- milestone_id（关联里程碑）
```

**9. 缺少库存管理**
```
问题：采购原材料 10 万，当月只用了 6 万，剩余 4 万是库存，不应该全部计入成本

业务需求：
- 采购入库：增加库存
- 生产领料：减少库存，计入成本
- 期末盘点：调整库存

建议：
新增表：
inventory（库存）
├── material_code（物料编码）
├── material_name（物料名称）
├── quantity（数量）
├── unit_cost（单位成本）
├── total_value（总价值）
└── warehouse_id（仓库）

inventory_transaction（库存流水）
├── type（in/out/adjust）
├── material_code
├── quantity
├── fact_id（关联采购/生产）
└── transaction_date

成本确认规则：
- 采购时：不计入成本，增加库存
- 领料时：计入成本，减少库存
```

#### 🟡 P1 - 重要问题

**10. 缺少工时管理**
```
问题：人力成本如何归属到项目？

业务需求：
- 员工填写工时单（项目A 8小时、项目B 4小时）
- 按工时比例分摊人力成本

建议：
新增表：
timesheet（工时单）
├── user_id
├── project_id
├── work_date
├── hours（工时）
└── task_description

人力成本归因策略：
- 按工时比例分摊到项目
```

**11. 缺少生产工单**
```
问题：制造业需要跟踪生产成本（料工费）

建议：
新增表：
work_order（生产工单）
├── order_no
├── product_code
├── quantity
├── material_cost（材料成本）
├── labor_cost（人工成本）
├── overhead_cost（制造费用）
└── total_cost
```

---

## 🏗️ 架构师技术评估（25% 权重）

### 对财务/业务需求的技术响应

#### 🔴 P0 技术方案

**1. 会计科目体系 → 多维度标签**
```
方案：
fact_event 不直接存 account_code，而是通过规则映射

fact_event
├── type（income/cost）
├── cost_category（fixed/variable）
├── business_type（sales/production/admin）
└── ...

mapping_rule（映射规则）
├── condition（JSON：type=income AND business_type=sales）
├── account_code（6001）
└── account_name（主营业务收入）

优点：
- 灵活：会计科目调整不影响历史数据
- 可追溯：保留映射历史
```

**2. 期间结账 → 状态机 + 事件溯源**
```java
PeriodClosingStateMachine
- open → closing（执行检查）
- closing → closed（通过检查）
- closed → open（反结账，需审批）

检查清单：
1. 所有 Fact 已审批
2. 归因计算完成
3. 指标计算完成
4. 无待处理异常

约束：
- Fact 录入时检查 period_closing.status
- 已结账期间拒绝写入
```

**3. 往来账管理 → 新增 Finance Context**
```
Finance Context（财务域）
├── Receivable（应收）
├── Payable（应付）
├── BankAccount（银行账户）
└── TaxDeclaration（税务申报）

依赖：
Finance → Fact（监听收入/成本事件）

流程：
1. 收入 Fact 创建 → 生成 Receivable
2. 收款 Fact 创建 → 更新 Receivable.received_amount
3. 成本 Fact 创建 → 生成 Payable
4. 付款 Fact 创建 → 更新 Payable.paid_amount
```

**4. 合同管理 → 新增 Contract Context**
```
Contract Context（合同域）
├── Contract（合同）
├── ContractMilestone（里程碑）
└── ContractExecutionService（执行服务）

依赖：
Contract → Fact（收入确认依据）

流程：
1. 合同签订 → 创建 Contract + Milestones
2. 里程碑完成 → 创建收入 Fact
3. 自动检查：已确认收入 ≤ 合同总额
```

**5. 库存管理 → 新增 Inventory Context**
```
Inventory Context（库存域）
├── Inventory（库存）
├── InventoryTransaction（库存流水）
└── InventoryValuationService（库存计价服务）

依赖：
Inventory → Fact（成本确认依据）

流程：
1. 采购 Fact → 增加库存（不计入成本）
2. 领料 → 减少库存 + 创建成本 Fact
3. 期末盘点 → 调整库存 + 创建调整 Fact
```

#### 🟡 P1 技术方案

**6. 固定资产 → 定时任务 + 折旧策略**
```java
DepreciationStrategy
- StraightLineStrategy（直线法）
- DecliningBalanceStrategy（余额递减法）

定时任务（每月1日）：
1. 查询所有固定资产
2. 计算当月折旧
3. 生成折旧费用 Fact
```

**7. 成本中心 → 维度建模**
```
fact_event 增加多维度：
├── org_unit_id（组织维度）
├── cost_center_id（成本中心维度）
├── project_id（项目维度）
├── counterparty_id（客户维度）
└── ...

归因时支持多维度聚合：
- 按组织聚合
- 按成本中心聚合
- 按项目聚合
```

---

## 🎨 产品经理意见（25% 权重）

### 产品影响评估

#### 🔴 P0 产品调整

**1. 首页改版（财务导向）**
```
旧设计：
- 收入/成本/利润

新设计：
┌─────────────────────────────────┐
│ 经营驾驶舱（2026-05）            │
├─────────────────────────────────┤
│ 【损益】                         │
│ 营业收入：500,000               │
│ 营业成本：300,000               │
│ 毛利润：200,000（40%）          │
│ 期间费用：50,000                │
│ 净利润：150,000（30%）          │
├─────────────────────────────────┤
│ 【资金】                         │
│ 应收账款：100,000（账期 30天）  │
│ 应付账款：80,000（账期 45天）   │
│ 银行余额：200,000               │
├─────────────────────────────────┤
│ 【税务】                         │
│ 本月应交增值税：15,000          │
│ 待抵扣进项税：5,000             │
└─────────────────────────────────┘
```

**2. 新增"财务管理"模块**
```
功能：
- 应收应付管理
- 银行对账
- 税务申报
- 期间结账
```

**3. 新增"合同管理"模块**
```
功能：
- 合同列表
- 合同执行进度
- 收入确认计划
```

**4. 新增"库存管理"模块**
```
功能：
- 库存查询
- 出入库记录
- 库存盘点
- 成本核算
```

#### 🟡 P1 产品优化

**5. 数据录入优化**
```
旧设计：
- 收入录入
- 成本录入

新设计：
- 销售开单（自动生成：收入 + 应收）
- 采购入库（自动生成：库存，不计成本）
- 生产领料（自动生成：成本，减库存）
- 收款登记（自动更新：应收）
- 付款登记（自动更新：应付）
```

**6. 决策建议增强**
```
新增财务类建议：
- 应收账款逾期预警
- 现金流预测
- 税务筹划建议
```

---

## 📊 四方评分结果

| 维度 | 架构师 | 业务 | 财务 | 产品 | 加权得分 |
|------|--------|------|------|------|----------|
| 财务合规性 | 6/10 | 7/10 | 3/10 | 5/10 | **5.25/10** |
| 业务完整性 | 7/10 | 4/10 | 6/10 | 5/10 | **5.5/10** |
| 技术可行性 | 8/10 | 7/10 | 6/10 | 7/10 | **7.0/10** |
| 产品体验 | 7/10 | 6/10 | 5/10 | 6/10 | **6.0/10** |
| **综合评分** | **7.0** | **6.0** | **5.0** | **5.75** | **5.94/10** |

**权重计算：** 7.0×25% + 6.0×25% + 5.0×25% + 5.75×25% = **5.94/10**

---

## 🎯 会议决议

### ❌ 不通过，需重大修订

**核心问题：**
1. 财务合规性不足（3/10）
2. 业务完整性不足（4/10）
3. 缺少关键财务模块（往来账、结账、税务）
4. 缺少关键业务模块（合同、库存）

---

## 📝 修订要求

### 必须修复（P0）- 不修复无法上线

1. ✅ 增加会计科目体系
2. ✅ 增加期间结账机制
3. ✅ 增加往来账管理（应收/应付）
4. ✅ 增加税务管理
5. ✅ 增加合同管理
6. ✅ 增加库存管理

### 建议增加（P1）- 影响体验

7. 固定资产管理
8. 成本中心
9. 工时管理
10. 银行对账

---

## 🏗️ 新增领域上下文

```
原有（7个）：
- Fact Context
- Attribution Context
- Metrics Context
- Budget Context
- Decision Context
- MasterData Context
- Approval Context

新增（3个）：
- Finance Context（财务域）★
  ├── Receivable / Payable
  ├── BankAccount
  ├── TaxDeclaration
  └── PeriodClosing

- Contract Context（合同域）★
  ├── Contract
  ├── ContractMilestone
  └── ContractExecution

- Inventory Context（库存域）★
  ├── Inventory
  ├── InventoryTransaction
  └── InventoryValuation

总计：10个上下文
```

---

## 📋 修订计划

**第一阶段（本周）：**
- 设计 Finance/Contract/Inventory Context
- 更新表结构（新增 10+ 张表）
- 更新领域模型文档

**第二阶段（下周）：**
- 更新 API 设计
- 调整产品原型

**第三阶段（下下周）：**
- 第五次评审（终审）

---

## 💡 财务专家建议

> "这个系统的定位要明确：是**管理会计系统**还是**财务会计系统**？
> 
> - 管理会计：侧重经营分析、成本控制、决策支持（当前设计）
> - 财务会计：侧重合规、报表、税务（缺失部分）
> 
> 建议：**两者结合**
> - 底层：财务会计（合规、完整）
> - 上层：管理会计（分析、决策）
> 
> 这样既能满足财务合规要求，又能提供管理决策支持。"

---

## 签字确认

- [ ] 架构师：________ （技术可行，但需增加3个上下文）
- [ ] 业务人员：________ （需要合同和库存管理）
- [ ] 财务：________ （必须增加往来账、结账、税务）
- [ ] 产品经理：________ （功能范围扩大，需调整MVP范围）
