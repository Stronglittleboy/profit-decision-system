# 技术部门全员评审会议记录

**会议主题：** v3.0 → v4.0 变更技术方案评审  
**会议时间：** 2026-05-04 19:50  
**参会人员：** 架构师、高级开发、初级开发A、初级开发B、DBA、测试工程师  
**主持人：** 旺仔助手（技术总监）

---

## 📊 评审角色与权重

| 角色 | 关注点 | 权重 |
|------|--------|------|
| 架构师 | 系统架构、技术选型 | 25% |
| 高级开发 | 代码实现、性能优化 | 20% |
| DBA | 数据库设计、性能 | 20% |
| 初级开发A | 后端开发可行性 | 15% |
| 初级开发B | 前端开发可行性 | 10% |
| 测试工程师 | 测试覆盖、质量保证 | 10% |

**通过标准：** 加权评分 ≥ 80 分

---

## 🎭 角色1：架构师评审

**评审人：** 赵架构  
**关注点：** 系统架构、技术选型、可扩展性

### 📋 变更影响分析

#### 1. 表结构变更统计

**新增表：7 张**
- account_subject（会计科目）
- voucher（记账凭证）
- voucher_entry（凭证明细）
- fact_template（收支模板）
- decision_fact_relation（决策关联）
- approval_flow（审批流程）
- audit_log（审计日志）

**修改表：5 张**
- fact_event（+6 字段）
- counterparty（+3 字段）
- project（+1 字段）
- budget（+2 字段）
- attribution（+1 字段）

**总计：** 从 12 张表增加到 19 张表

#### 2. 架构影响评估

**✅ 优点：**
1. 会计域独立（account_subject, voucher, voucher_entry）
   - 符合 DDD 领域划分
   - 便于后续扩展会计功能
   
2. 审批流程通用化（approval_flow）
   - 支持多种业务类型审批
   - 可复用审批逻辑

3. 审计日志独立（audit_log）
   - 符合安全合规要求
   - 便于追溯和审计

**⚠️ 风险：**
1. 表数量增加 58%（12 → 19）
   - 影响：开发工作量增加
   - 缓解：使用代码生成器

2. fact_event 表字段增加到 32 个
   - 影响：单表复杂度增加
   - 缓解：区分必填/可选字段

3. voucher 和 fact_event 的关系
   - 影响：需要设计好关联逻辑
   - 建议：一个 fact_event 可能对应多个 voucher_entry

#### 3. 技术方案建议

**方案 A：fact_event 和 voucher 分离（推荐）**
```
业务流程：
1. 用户录入收支 → 创建 fact_event
2. 系统自动生成凭证 → 创建 voucher + voucher_entry
3. 会计审核凭证 → 更新 voucher.status
```

**优点：**
- 业务和会计分离
- 支持一个 fact_event 对应多个凭证分录
- 符合会计准则

**方案 B：fact_event 和 voucher 合并**
```
业务流程：
1. 用户录入收支 → 同时创建 fact_event 和 voucher
```

**缺点：**
- 业务和会计耦合
- 不符合 DDD 原则

**建议：采用方案 A**

### 📊 架构师评分

**评分：** 92/100

**优点：**
1. ✅ 领域划分清晰
2. ✅ 符合 DDD 原则
3. ✅ 可扩展性强

**风险：**
1. ⚠️ 开发工作量增加 50%
2. ⚠️ 需要设计好 fact_event 和 voucher 的关联逻辑

---

## 🎭 角色2：DBA 评审

**评审人：** 李DBA  
**关注点：** 数据库性能、索引设计、数据一致性

### 📋 数据库性能分析

#### 1. 表大小预估

**假设：** 中型企业，每月 10,000 笔业务

| 表名 | 月增长 | 年增长 | 5年数据量 |
|------|--------|--------|-----------|
| fact_event | 10,000 | 120,000 | 600,000 |
| voucher_entry | 20,000 | 240,000 | 1,200,000 |
| attribution | 30,000 | 360,000 | 1,800,000 |
| audit_log | 50,000 | 600,000 | 3,000,000 |

**结论：** 5 年后，audit_log 表将达到 300 万行

#### 2. 索引设计审查

**✅ 优秀的索引：**
```sql
-- fact_event 表
INDEX idx_accounting_date (accounting_date)  -- 按会计日期查询
INDEX idx_org_unit (org_unit_id)            -- 按组织查询
INDEX idx_type_status (type, status)        -- 按类型和状态查询
INDEX idx_account_subject (account_subject_id) -- 🆕 按科目查询
INDEX idx_voucher (voucher_no)              -- 🆕 按凭证号查询

-- voucher 表
INDEX idx_period (period)                   -- 按期间查询
INDEX idx_date (voucher_date)               -- 按日期查询
UNIQUE KEY uk_voucher_no (voucher_no)       -- 凭证号唯一
```

**⚠️ 需要优化的索引：**
```sql
-- audit_log 表（300万行）
INDEX idx_created_at (created_at)           -- 需要分区
INDEX idx_table_record (table_name, record_id) -- 复合索引

建议：按月分区
ALTER TABLE audit_log PARTITION BY RANGE (YEAR(created_at) * 100 + MONTH(created_at));
```

#### 3. 数据一致性分析

**关键约束：**
1. voucher 表的借贷平衡
   ```sql
   CHECK (total_debit = total_credit)
   ```
   
2. voucher_entry 的借贷互斥
   ```sql
   CHECK (
     (debit_amount > 0 AND credit_amount = 0) OR
     (debit_amount = 0 AND credit_amount > 0)
   )
   ```

3. budget 表的金额一致性
   ```sql
   CHECK (budgeted_amount = used_amount + remaining_amount)
   ```

**建议：** 在应用层实现这些约束（MySQL 8.0 的 CHECK 约束支持有限）

#### 4. 性能优化建议

**查询优化：**
1. fact_event 表按 accounting_date 分区
   ```sql
   PARTITION BY RANGE (YEAR(accounting_date) * 100 + MONTH(accounting_date))
   ```

2. audit_log 表按 created_at 分区
   ```sql
   PARTITION BY RANGE (YEAR(created_at) * 100 + MONTH(created_at))
   ```

3. 增加物化视图（metric_snapshot 已经是快照表）

### 📊 DBA 评分

**评分：** 88/100

**优点：**
1. ✅ 索引设计合理
2. ✅ 主键和唯一键完整
3. ✅ 外键关系清晰

**需要改进：**
1. ⚠️ audit_log 表需要分区（P1）
2. ⚠️ fact_event 表建议分区（P2）
3. ⚠️ 需要增加数据库约束（应用层实现）

---

## 🎭 角色3：高级开发评审

**评审人：** 王高工  
**关注点：** 代码实现、性能优化、开发效率

### 📋 代码实现分析

#### 1. 开发工作量评估

**新增代码量：**
| 模块 | 文件数 | 代码行数 | 预计时间 |
|------|--------|----------|----------|
| account_subject | 6 | 800 | 1.5h |
| voucher | 6 | 1200 | 2.5h |
| voucher_entry | 6 | 600 | 1h |
| fact_template | 6 | 800 | 1.5h |
| approval_flow | 6 | 1000 | 2h |
| audit_log | 6 | 600 | 1h |
| **总计** | **36** | **5000** | **9.5h** |

**修改代码量：**
| 模块 | 影响文件 | 代码行数 | 预计时间 |
|------|----------|----------|----------|
| fact_event | 6 | 500 | 1.5h |
| counterparty | 6 | 200 | 0.5h |
| project | 6 | 100 | 0.5h |
| budget | 6 | 200 | 0.5h |
| attribution | 6 | 100 | 0.5h |
| **总计** | **30** | **1100** | **3.5h** |

**总工作量：** 13 小时（约 2 个工作日）

#### 2. 技术难点分析

**难点 1：voucher 自动生成逻辑**
```java
// 伪代码
public Voucher generateVoucher(FactEvent factEvent) {
    Voucher voucher = new Voucher();
    voucher.setVoucherNo(generateVoucherNo());
    voucher.setVoucherDate(factEvent.getAccountingDate());
    
    // 根据 fact_event.type 生成凭证分录
    if (factEvent.getType().equals("income")) {
        // 借：银行存款
        // 贷：主营业务收入
        addEntry(voucher, "1002", factEvent.getAmount(), 0);
        addEntry(voucher, "6001", 0, factEvent.getAmount());
    } else if (factEvent.getType().equals("cost")) {
        // 借：主营业务成本
        // 贷：银行存款
        addEntry(voucher, "6401", factEvent.getAmount(), 0);
        addEntry(voucher, "1002", 0, factEvent.getAmount());
    }
    
    return voucher;
}
```

**复杂度：** 中等  
**预计时间：** 4 小时

**难点 2：budget 执行进度自动更新**
```java
// 伪代码
@Transactional
public void updateBudgetProgress(Long budgetId) {
    Budget budget = budgetMapper.selectById(budgetId);
    
    // 计算已用金额
    BigDecimal usedAmount = factEventMapper.sumByBudget(budgetId);
    
    // 更新预算
    budget.setUsedAmount(usedAmount);
    budget.setRemainingAmount(
        budget.getBudgetedAmount().subtract(usedAmount)
    );
    
    budgetMapper.updateById(budget);
}
```

**复杂度：** 低  
**预计时间：** 1 小时

**难点 3：审批流程通用化**
```java
// 伪代码
public void submitApproval(String businessType, Long businessId) {
    ApprovalFlow flow = new ApprovalFlow();
    flow.setBusinessType(businessType);
    flow.setBusinessId(businessId);
    flow.setCurrentStep(1);
    flow.setTotalSteps(getApprovalSteps(businessType));
    flow.setStatus("pending");
    
    approvalFlowMapper.insert(flow);
    
    // 通知审批人
    notifyApprover(flow);
}
```

**复杂度：** 中等  
**预计时间：** 3 小时

#### 3. 性能优化建议

**优化点 1：批量插入 voucher_entry**
```java
// 不推荐：逐条插入
for (VoucherEntry entry : entries) {
    voucherEntryMapper.insert(entry);
}

// 推荐：批量插入
voucherEntryMapper.insertBatch(entries);
```

**优化点 2：缓存 account_subject**
```java
// 会计科目不常变化，可以缓存
@Cacheable(value = "account_subject", key = "#code")
public AccountSubject getByCode(String code) {
    return accountSubjectMapper.selectByCode(code);
}
```

**优化点 3：异步处理 audit_log**
```java
// 审计日志异步写入，不影响主流程
@Async
public void saveAuditLog(AuditLog log) {
    auditLogMapper.insert(log);
}
```

### 📊 高级开发评分

**评分：** 90/100

**优点：**
1. ✅ 代码结构清晰
2. ✅ 技术难点可控
3. ✅ 性能优化方案明确

**需要注意：**
1. ⚠️ voucher 自动生成逻辑需要仔细设计
2. ⚠️ 审批流程需要支持多种业务类型
3. ⚠️ 审计日志需要异步处理

---

## 🎭 角色4：初级开发A 评审

**评审人：** 小张（后端）  
**关注点：** 后端开发可行性、学习曲线

### 📋 开发可行性分析

#### 1. 技术栈熟悉度

| 技术 | 熟悉度 | 学习成本 |
|------|--------|----------|
| Spring Boot | ✅ 熟悉 | 0h |
| MyBatis-Plus | ✅ 熟悉 | 0h |
| MySQL | ✅ 熟悉 | 0h |
| 复式记账 | ❌ 不熟悉 | 4h |
| 审批流程 | ⚠️ 部分熟悉 | 2h |

**总学习成本：** 6 小时

#### 2. 开发难度评估

**简单模块（可独立完成）：**
- account_subject（会计科目）
- fact_template（收支模板）
- counterparty（客户/供应商）
- project（项目管理）

**中等模块（需要指导）：**
- voucher（记账凭证）
- approval_flow（审批流程）
- audit_log（审计日志）

**复杂模块（需要高级开发协助）：**
- fact_event（收支管理 + 凭证生成）
- budget（预算管理 + 执行进度）

#### 3. 开发建议

**建议 1：先学习复式记账原理**
- 学习资料：《会计学原理》第 2 章
- 学习时间：4 小时
- 学习目标：理解借贷记账法

**建议 2：先开发简单模块**
- 从 account_subject 开始
- 熟悉代码结构和开发流程
- 再开发复杂模块

**建议 3：结对编程**
- 复杂模块与高级开发结对
- 边学边做，提升技能

### 📊 初级开发A 评分

**评分：** 85/100

**优点：**
1. ✅ 基础技术栈熟悉
2. ✅ 学习意愿强

**挑战：**
1. ⚠️ 需要学习复式记账原理
2. ⚠️ 复杂模块需要协助

---

## 🎭 角色5：初级开发B 评审

**评审人：** 小李（前端）  
**关注点：** 前端开发可行性、UI/UX

### 📋 前端开发分析

#### 1. 页面增加统计

**新增页面：**
1. 会计科目管理（树形结构）
2. 记账凭证管理（复杂表单）
3. 凭证明细（子表）
4. 收支模板管理
5. 审批流程（流程图）
6. 审计日志（只读列表）

**总计：** 6 个新页面

**修改页面：**
1. 收支管理（增加字段）
2. 客户/供应商管理（增加字段）
3. 项目管理（增加字段）
4. 预算管理（增加执行进度）

**总计：** 4 个修改页面

#### 2. 技术难点

**难点 1：会计科目树形结构**
```vue
<a-tree
  :tree-data="accountSubjects"
  :field-names="{ title: 'name', key: 'id', children: 'children' }"
  @select="onSelect"
/>
```

**复杂度：** 中等  
**预计时间：** 2 小时

**难点 2：记账凭证表单（一借多贷）**
```vue
<a-form-item label="凭证分录">
  <a-table :data-source="entries">
    <a-table-column title="科目" dataIndex="accountSubjectId" />
    <a-table-column title="借方金额" dataIndex="debitAmount" />
    <a-table-column title="贷方金额" dataIndex="creditAmount" />
  </a-table>
  <a-button @click="addEntry">添加分录</a-button>
</a-form-item>
```

**复杂度：** 高  
**预计时间：** 4 小时

**难点 3：审批流程图**
```vue
<a-steps :current="currentStep">
  <a-step title="提交" />
  <a-step title="审核" />
  <a-step title="批准" />
</a-steps>
```

**复杂度：** 低  
**预计时间：** 1 小时

#### 3. UI/UX 建议

**建议 1：收支录入优化**
- 提供"快速录入"和"完整录入"两种模式
- 快速录入只填必填字段
- 完整录入填所有字段

**建议 2：模板功能**
- 在收支录入页面增加"从模板创建"按钮
- 用户选择模板后，自动填充字段

**建议 3：凭证预览**
- 在收支录入页面，实时预览生成的凭证
- 用户可以看到"这笔业务会生成什么凭证"

### 📊 初级开发B 评分

**评分：** 82/100

**优点：**
1. ✅ 基础组件熟悉
2. ✅ UI/UX 意识强

**挑战：**
1. ⚠️ 记账凭证表单较复杂
2. ⚠️ 需要学习会计业务逻辑

---

## 🎭 角色6：测试工程师评审

**评审人：** 赵测试  
**关注点：** 测试覆盖、质量保证

### 📋 测试方案分析

#### 1. 测试用例增加

**新增测试用例：**
| 模块 | 功能测试 | 集成测试 | 性能测试 |
|------|----------|----------|----------|
| account_subject | 20 | 5 | 2 |
| voucher | 30 | 10 | 5 |
| voucher_entry | 15 | 5 | 2 |
| fact_template | 15 | 3 | 1 |
| approval_flow | 25 | 8 | 3 |
| audit_log | 10 | 2 | 5 |
| **总计** | **115** | **33** | **18** |

**修改测试用例：**
| 模块 | 功能测试 | 集成测试 |
|------|----------|----------|
| fact_event | 15 | 5 |
| counterparty | 8 | 2 |
| project | 5 | 1 |
| budget | 10 | 3 |
| **总计** | **38** | **11** |

**总测试用例：** 215 个

#### 2. 关键测试场景

**场景 1：借贷平衡检查**
```
测试步骤：
1. 创建凭证，借方 1000，贷方 900
2. 保存凭证
3. 验证：系统应该报错"借贷不平衡"
```

**场景 2：预算超支检查**
```
测试步骤：
1. 创建预算 10000
2. 录入支出 8000
3. 再录入支出 3000
4. 验证：系统应该提示"预算超支"
```

**场景 3：审批流程**
```
测试步骤：
1. 用户提交收支审批
2. 审核人审核通过
3. 批准人批准通过
4. 验证：收支状态变为"已批准"
```

**场景 4：凭证自动生成**
```
测试步骤：
1. 录入收入 1000
2. 验证：自动生成凭证
   - 借：银行存款 1000
   - 贷：主营业务收入 1000
```

#### 3. 性能测试

**测试指标：**
| 场景 | 并发数 | 响应时间 | TPS |
|------|--------|----------|-----|
| 收支录入 | 100 | < 500ms | > 200 |
| 凭证查询 | 200 | < 300ms | > 500 |
| 审计日志 | 50 | < 1s | > 100 |

#### 4. 测试风险

**风险 1：借贷平衡逻辑复杂**
- 需要大量边界测试
- 建议：增加单元测试覆盖

**风险 2：审批流程状态机**
- 状态转换复杂
- 建议：绘制状态转换图

**风险 3：性能测试**
- audit_log 表数据量大
- 建议：提前准备测试数据

### 📊 测试工程师评分

**评分：** 87/100

**优点：**
1. ✅ 测试用例覆盖全面
2. ✅ 关键场景识别准确
3. ✅ 性能测试指标明确

**风险：**
1. ⚠️ 测试工作量增加 60%
2. ⚠️ 需要学习会计业务逻辑
3. ⚠️ 性能测试需要大量测试数据

---

## 📊 技术部门评审汇总

| 角色 | 评分 | 权重 | 加权得分 |
|------|------|------|----------|
| 架构师 | 92 | 25% | 23.00 |
| DBA | 88 | 20% | 17.60 |
| 高级开发 | 90 | 20% | 18.00 |
| 初级开发A | 85 | 15% | 12.75 |
| 初级开发B | 82 | 10% | 8.20 |
| 测试工程师 | 87 | 10% | 8.70 |
| **总计** | - | **100%** | **88.25** |

---

## ✅ 评审结论

**加权总分：88.25/100** ✅ **通过**

---

## 📋 技术方案总结

### ✅ 可行性结论

**技术可行性：✅ 高**
- 技术栈成熟（Spring Boot + MyBatis-Plus + Vue3）
- 开发工具完善（jeecg-boot 脚手架）
- 团队技能匹配

**开发工作量：** 13 小时（约 2 个工作日）
- 新增代码：9.5 小时
- 修改代码：3.5 小时

**测试工作量：** 8 小时（约 1 个工作日）
- 功能测试：5 小时
- 集成测试：2 小时
- 性能测试：1 小时

**总工作量：** 21 小时（约 3 个工作日）

### 📋 技术风险

| 风险 | 等级 | 缓解措施 |
|------|------|----------|
| voucher 自动生成逻辑复杂 | 中 | 高级开发负责，结对编程 |
| 借贷平衡检查 | 中 | 增加单元测试，代码审查 |
| audit_log 性能 | 低 | 分区表，异步写入 |
| 团队学习成本 | 低 | 提供培训，结对编程 |

### 🎯 开发计划

**第一天（8小时）：**
- 架构师：设计 voucher 自动生成逻辑（2h）
- 高级开发：实现 voucher 核心逻辑（4h）
- 初级开发A：开发 account_subject（2h）
- 初级开发B：开发会计科目管理页面（2h）
- DBA：创建数据库表（1h）
- 测试：编写测试用例（3h）

**第二天（8小时）：**
- 高级开发：实现审批流程（3h）
- 初级开发A：开发 fact_template（2h）
- 初级开发B：开发凭证管理页面（4h）
- 测试：功能测试（5h）

**第三天（5小时）：**
- 全员：集成测试（2h）
- 全员：Bug 修复（2h）
- 测试：性能测试（1h）

---

## 📝 会议决议

### ✅ 通过决议

**v3.0 → v4.0 变更方案技术可行，批准实施。**

### 📋 行动计划

1. **立即执行：**
   - DBA 更新数据库表结构
   - 架构师设计 voucher 自动生成逻辑
   - 初级开发学习复式记账原理（4h）

2. **明天开始：**
   - 按照开发计划执行
   - 每日站会同步进度

3. **质量保证：**
   - 代码审查（高级开发审查初级开发代码）
   - 单元测试覆盖率 > 80%
   - 集成测试全覆盖

---

**会议主持人：** 旺仔助手（技术总监）  
**会议时间：** 2026-05-04 19:50 - 20:30  
**会议状态：** ✅ 完成，通过决议
