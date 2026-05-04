# 技术实施方案会议纪要

**时间：** 2026-05-01 深夜  
**参会人员：** 架构师（40%）、高级开发（30%）、初级开发A（15%）、初级开发B（15%）  
**议题：** 基于全量功能 + 渐进开放策略，制定高效开发方案

---

## 📋 会议背景

**产品要求：**
- 2个月上线基础版（8个模块全部呈现）
- 核心功能完整（财务管理）
- 高级功能占位（经营分析/库存/决策）

**团队配置：**
- 1名架构师（负责架构设计、核心模块）
- 1名高级开发（负责复杂业务逻辑）
- 2名初级开发（负责 CRUD、前端页面）

**挑战：**
- 时间紧（8周）
- 功能多（8个模块）
- 人力有限（4人）

---

## 🏗️ 架构师方案（40% 权重）

### 核心观点：技术选型决定开发效率

#### 技术栈选型

**后端：Spring Boot + 代码生成器**
```
基础框架：ruoyi-vue-pro（开源）
- 优点：
  ✅ 自带代码生成器（表 → CRUD 代码）
  ✅ 自带权限管理、用户管理
  ✅ 自带操作日志、审计日志
  ✅ 开箱即用，节省 2 周开发时间

- 技术栈：
  Spring Boot 2.7
  MyBatis-Plus（简化 SQL）
  Redis（缓存）
  MySQL 8.0
```

**前端：Vue3 + 低代码**
```
基础框架：ruoyi-vue-pro 前端
- 优点：
  ✅ 自带表单生成器
  ✅ 自带表格组件
  ✅ 自带权限控制
  ✅ Element Plus UI 库

- 技术栈：
  Vue 3
  Vite
  Element Plus
  Pinia（状态管理）
```

**数据库：MySQL + 标准化设计**
```
策略：
- 表名统一前缀（fact_/finance_/business_）
- 字段命名规范（created_at/updated_at）
- 预留扩展字段（metadata JSON）
- 软删除（is_deleted）
```

---

#### 架构分层（DDD 简化版）

```
┌─────────────────────────────────────────┐
│          前端层（Vue3）                  │
│  - 页面组件（8个模块）                   │
│  - 通用组件（表单/表格/图表）            │
└─────────────────────────────────────────┘
              ↓ HTTP
┌─────────────────────────────────────────┐
│          Controller 层                   │
│  - FactController                       │
│  - ReceivableController                 │
│  - PeriodClosingController              │
│  - ...                                  │
└─────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────┐
│          Service 层（业务逻辑）          │
│  - FactService                          │
│  - ReceivableService                    │
│  - PeriodClosingService                 │
│  - ...                                  │
└─────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────┐
│          Mapper 层（数据访问）           │
│  - FactMapper（MyBatis-Plus）           │
│  - ReceivableMapper                     │
│  - ...                                  │
└─────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────┐
│          MySQL 数据库                    │
└─────────────────────────────────────────┘
```

**简化原则：**
- ❌ 不做：复杂的 DDD 聚合根、值对象、领域事件
- ✅ 只做：清晰的分层、Service 封装业务逻辑
- 理由：4人团队，2个月时间，过度设计会拖慢进度

---

#### 代码生成策略

**自动生成（80% 代码）：**
```
使用 ruoyi 代码生成器：
1. 设计数据库表
2. 配置生成规则
3. 一键生成：
   - Entity（实体类）
   - Mapper（数据访问）
   - Service（业务逻辑）
   - Controller（接口）
   - Vue 页面（列表/表单/详情）

生成的代码包括：
✅ CRUD 接口
✅ 分页查询
✅ 导入导出
✅ 数据校验
✅ 操作日志

节省时间：每个模块节省 2-3 天
```

**手写代码（20% 核心逻辑）：**
```
需要手写的部分：
1. 期间结账逻辑（复杂业务规则）
2. 应收应付计算（关联多表）
3. 报表生成（复杂 SQL）
4. 占位符页面（前端）
```

---

#### 数据库设计策略

**P0 表（立即创建，2个月内使用）：**
```sql
-- 事实域
fact_event（收支记录）
accounting_subject（会计科目）

-- 财务域
receivable（应收账款）
payable（应付账款）
period_closing（期间结账）

-- 主数据域
counterparty（客户/供应商）
org_unit（组织架构）
user（用户）

-- 业务域
contract（合同 - 基础版）
project（项目 - 基础版）

-- 系统域
audit_log（审计日志 - ruoyi 自带）
```

**P1/P2 表（预留，3-8个月后使用）：**
```sql
-- 归因域（P1）
attribution（归因记录）
attribution_rule（归因规则）

-- 指标域（P1）
metric_snapshot（指标快照）

-- 预算域（P1）
budget（预算）
budget_adjustment（预算调整）

-- 库存域（P2）
inventory（库存）
inventory_transaction（库存流水）

-- 决策域（P2）
decision_fact_relation（决策关联）
action_record（执行记录）
```

**策略：**
- P0 表立即创建（保证 2 个月可用）
- P1/P2 表预留字段（fact_event 表设计完整）
- 避免后期修改表结构（影响已有数据）

---

#### 开发环境搭建

**标准化开发环境：**
```
1. Docker Compose 一键启动：
   - MySQL 8.0
   - Redis 6.0
   - Nginx（前端代理）

2. 代码仓库：
   - 后端：profit-system-backend
   - 前端：profit-system-frontend
   - 数据库：profit-system-db（SQL 脚本）

3. 开发规范：
   - Git Flow 分支管理
   - 代码 Review（架构师审核）
   - 每日站会（15分钟同步进度）
```

---

## 💻 高级开发方案（30% 权重）

### 核心观点：复杂业务逻辑需要精细设计

#### 负责模块

**1. 期间结账（最复杂）**
```java
PeriodClosingService
├── checkBeforeClosing()  // 结账前检查
│   ├── 检查所有 Fact 已审批
│   ├── 检查应收应付已核对
│   └── 检查无异常数据
├── executeClosing()      // 执行结账
│   ├── 锁定期间（更新 period_closing.status）
│   ├── 生成结账快照
│   └── 发送通知
└── reverseClosing()      // 反结账
    ├── 检查权限（需审批）
    ├── 解锁期间
    └── 记录操作日志
```

**2. 应收应付管理（关联复杂）**
```java
ReceivableService
├── createFromFact()      // 从收入 Fact 生成应收
├── recordPayment()       // 记录收款
│   ├── 更新 received_amount
│   ├── 计算 outstanding_amount
│   └── 判断是否逾期
└── getOverdueList()      // 逾期预警

PayableService
├── createFromFact()      // 从成本 Fact 生成应付
├── recordPayment()       // 记录付款
└── getPaymentPlan()      // 付款计划
```

**3. 报表生成（复杂 SQL）**
```java
ReportService
├── getIncomeStatement()  // 收支明细表
│   └── 复杂 SQL：多表关联 + 分组聚合
├── getReceivableReport() // 应收应付表
│   └── 复杂 SQL：计算账龄
└── getAccountBalance()   // 科目余额表
    └── 复杂 SQL：按科目汇总
```

---

#### 技术难点解决方案

**难点1：期间结账的并发控制**
```java
问题：多人同时结账怎么办？

方案：分布式锁（Redis）
@Transactional
public void executeClosing(String period) {
    String lockKey = "period_closing:" + period;
    RLock lock = redissonClient.getLock(lockKey);
    
    try {
        // 尝试获取锁（最多等待 10 秒）
        if (lock.tryLock(10, 30, TimeUnit.SECONDS)) {
            // 执行结账逻辑
            doClosing(period);
        } else {
            throw new BusinessException("结账中，请稍后重试");
        }
    } finally {
        lock.unlock();
    }
}
```

**难点2：应收应付的数据一致性**
```java
问题：收入 Fact 和应收账款如何保持一致？

方案：事务 + 事件
@Transactional
public void createIncomeFact(FactCreateDTO dto) {
    // 1. 创建收入 Fact
    FactEvent fact = factMapper.insert(dto);
    
    // 2. 创建应收账款
    Receivable receivable = new Receivable();
    receivable.setFactId(fact.getId());
    receivable.setTotalAmount(fact.getAmount());
    receivable.setOutstandingAmount(fact.getAmount());
    receivableMapper.insert(receivable);
    
    // 3. 发布事件（后续扩展用）
    eventPublisher.publish(new FactCreatedEvent(fact));
}
```

**难点3：报表性能优化**
```java
问题：数据量大时，报表查询慢

方案：缓存 + 异步生成
@Cacheable(value = "report", key = "#period")
public IncomeStatementVO getIncomeStatement(String period) {
    // 查询数据库
    return reportMapper.selectIncomeStatement(period);
}

// 定时任务：每日凌晨预生成报表
@Scheduled(cron = "0 0 2 * * ?")
public void preGenerateReports() {
    String yesterday = DateUtil.yesterday();
    getIncomeStatement(yesterday); // 触发缓存
}
```

---

## 👨‍💻 初级开发A方案（15% 权重）

### 核心观点：标准 CRUD 快速交付

#### 负责模块

**1. 客户/供应商管理（简单 CRUD）**
```
功能：
- 列表查询（分页/筛选/排序）
- 新增/编辑/删除
- 导入导出

实现方式：
✅ 100% 代码生成
- 设计表结构
- 配置生成规则
- 一键生成代码
- 微调 UI
```

**2. 会计科目管理（树形结构）**
```
功能：
- 树形展示（父子关系）
- 新增/编辑/删除
- 科目启用/停用

实现方式：
⚠️ 80% 代码生成 + 20% 手写
- 生成基础 CRUD
- 手写树形查询逻辑
```

**3. 合同管理（基础版）**
```
功能：
- 合同登记（不关联收入确认）
- 合同查询
- 合同状态管理

实现方式：
✅ 100% 代码生成
```

**4. 项目管理（基础版）**
```
功能：
- 项目登记（不关联成本归因）
- 项目查询
- 项目状态管理

实现方式：
✅ 100% 代码生成
```

---

#### 开发流程

```
Day 1-2: 学习 ruoyi 框架
Day 3-4: 客户/供应商管理（生成 + 测试）
Day 5-6: 会计科目管理（生成 + 手写树形逻辑）
Day 7-8: 合同管理（生成 + 测试）
Day 9-10: 项目管理（生成 + 测试）
```

---

## 👨‍💻 初级开发B方案（15% 权重）

### 核心观点：前端页面快速搭建

#### 负责模块

**1. 占位符页面（5个）**
```
页面：
- 成本分析（占位）
- 客户分析（占位）
- 项目分析（占位）
- 库存管理（占位）
- 智能决策（占位）

实现方式：
✅ 纯前端，无后端接口
- 复用统一的占位符组件
- 配置不同的提示文案
- 配置不同的图标

代码量：每个页面 50 行
总计：250 行（1天完成）
```

**2. 工作台（首页）**
```
功能：
- 经营驾驶舱（卡片展示）
- 待办事项（列表）
- 快捷入口（图标导航）

实现方式：
⚠️ 50% 组件库 + 50% 手写
- 使用 Element Plus 卡片组件
- 调用后端接口获取数据
- ECharts 图表展示

代码量：500 行（3天完成）
```

**3. 基础报表页面**
```
功能：
- 收支明细表（表格 + 导出）
- 应收应付表（表格 + 导出）
- 科目余额表（表格 + 导出）

实现方式：
✅ 80% 代码生成 + 20% 手写
- 生成基础表格页面
- 手写导出功能
- 手写筛选条件

代码量：每个页面 300 行
总计：900 行（5天完成）
```

**4. 系统设置页面**
```
功能：
- 组织架构（树形 + 编辑）
- 用户管理（列表 + 编辑）
- 权限管理（角色 + 菜单）

实现方式：
✅ 100% 使用 ruoyi 自带页面
- 直接复用，无需开发
```

---

#### 开发流程

```
Day 1-2: 学习 Vue3 + Element Plus
Day 3: 占位符页面（5个）
Day 4-6: 工作台（首页）
Day 7-11: 基础报表页面（3个）
Day 12-14: 联调测试
```

---

## 📊 四方评分结果

| 维度 | 架构师 | 高级开发 | 初级A | 初级B | 加权得分 |
|------|--------|----------|-------|-------|----------|
| 技术可行性 | 9/10 | 9/10 | 8/10 | 8/10 | **8.7/10** |
| 开发效率 | 9/10 | 8/10 | 9/10 | 9/10 | **8.8/10** |
| 代码质量 | 8/10 | 9/10 | 7/10 | 7/10 | **8.0/10** |
| 风险控制 | 9/10 | 8/10 | 8/10 | 8/10 | **8.4/10** |
| **综合评分** | **8.75** | **8.5** | **8.0** | **8.0** | **8.5/10** |

**权重计算：** 8.75×40% + 8.5×30% + 8.0×15% + 8.0×15% = **8.5/10**

---

## 🎯 会议决议

### ✅ 通过 - 采用代码生成 + 分工协作策略

**核心决策：**
1. ✅ 技术栈：ruoyi-vue-pro（自带代码生成器）
2. ✅ 架构：简化 DDD（清晰分层，不过度设计）
3. ✅ 分工：架构师（核心）+ 高级（复杂）+ 初级（CRUD）
4. ✅ 效率：80% 代码生成 + 20% 手写核心逻辑

---

## 📋 任务分工（8周）

### Week 1-2: 基础搭建

**架构师：**
- ✅ 搭建 ruoyi-vue-pro 框架
- ✅ 设计完整数据库表结构（P0 + P1/P2 预留）
- ✅ 配置代码生成规则
- ✅ 搭建 Docker 开发环境

**高级开发：**
- ✅ 学习 ruoyi 框架
- ✅ 设计期间结账逻辑
- ✅ 设计应收应付逻辑

**初级开发A：**
- ✅ 学习 ruoyi 框架
- ✅ 学习 MyBatis-Plus

**初级开发B：**
- ✅ 学习 Vue3 + Element Plus
- ✅ 学习 ruoyi 前端框架

---

### Week 3-4: 核心功能开发

**架构师：**
- ✅ 收支管理（Fact 模块）
- ✅ 代码 Review

**高级开发：**
- ✅ 应收应付管理
- ✅ 期间结账（核心逻辑）

**初级开发A：**
- ✅ 客户/供应商管理（代码生成）
- ✅ 会计科目管理（代码生成 + 树形逻辑）

**初级开发B：**
- ✅ 收支管理前端页面
- ✅ 应收应付前端页面
- ✅ 占位符页面（5个）

---

### Week 5-6: 业务功能开发

**架构师：**
- ✅ 发票管理
- ✅ 代码 Review

**高级开发：**
- ✅ 报表生成（复杂 SQL）
- ✅ 报表缓存优化

**初级开发A：**
- ✅ 合同管理（基础版）
- ✅ 项目管理（基础版）

**初级开发B：**
- ✅ 工作台（首页）
- ✅ 基础报表页面（3个）

---

### Week 7: 测试 + 优化

**全员：**
- ✅ 功能测试
- ✅ 性能测试
- ✅ Bug 修复
- ✅ 代码优化

---

### Week 8: 部署 + 试运行

**架构师：**
- ✅ 生产环境部署
- ✅ 数据库初始化
- ✅ 监控配置

**高级开发：**
- ✅ 数据迁移脚本
- ✅ 性能监控

**初级开发A/B：**
- ✅ 用户培训文档
- ✅ 操作手册

---

## 🛠️ 技术实施细节

### 代码生成配置

**表配置示例（counterparty）：**
```yaml
# 代码生成配置
tableName: counterparty
businessName: counterparty
className: Counterparty
packageName: com.profit.business
moduleName: business
author: profit-team

# 字段配置
columns:
  - columnName: id
    javaType: Long
    javaField: id
    isPk: true
    isIncrement: true
  
  - columnName: name
    javaType: String
    javaField: name
    isRequired: true
    queryType: LIKE
    htmlType: input
  
  - columnName: type
    javaType: String
    javaField: type
    isRequired: true
    queryType: EQ
    htmlType: select
    dictType: counterparty_type

# 生成选项
genType: 0  # 0=zip压缩包 1=自定义路径
tplCategory: crud  # crud=单表 tree=树表
```

---

### 数据库表结构（P0 核心表）

**完整 SQL 见：**
`database/schema-final-p0.sql`

**核心表清单：**
```
1. fact_event（收支记录）
2. accounting_subject（会计科目）
3. receivable（应收账款）
4. payable（应付账款）
5. period_closing（期间结账）
6. counterparty（客户/供应商）
7. contract（合同 - 基础版）
8. project（项目 - 基础版）
9. org_unit（组织架构）
10. user（用户 - ruoyi 自带）
```

---

### API 接口规范

**RESTful 风格：**
```
GET    /api/v1/facts          # 查询列表
GET    /api/v1/facts/{id}     # 查询详情
POST   /api/v1/facts          # 新增
PUT    /api/v1/facts/{id}     # 修改
DELETE /api/v1/facts/{id}     # 删除
POST   /api/v1/facts/export   # 导出
POST   /api/v1/facts/import   # 导入
```

**响应格式：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

---

## 💡 关键成功因素

### 1. 代码生成器是核心
```
优势：
- 节省 80% 开发时间
- 代码风格统一
- 减少低级错误

关键：
- 表结构设计要规范
- 生成配置要准确
- 生成后微调即可
```

### 2. 分工明确是关键
```
架构师：
- 核心模块（Fact/结账）
- 复杂逻辑设计
- 代码 Review

高级开发：
- 复杂业务（应收应付/报表）
- 性能优化
- 技术难点攻关

初级开发：
- 标准 CRUD（客户/供应商/合同/项目）
- 前端页面（占位符/报表/工作台）
- 测试 + 文档
```

### 3. 每日站会是保障
```
时间：每天早上 10:00
时长：15 分钟
内容：
- 昨天完成了什么
- 今天计划做什么
- 遇到什么问题

目的：
- 同步进度
- 及时发现风险
- 互相协作
```

---

## 🚀 下一步行动

立即开始：

1. **架构师：搭建 ruoyi-vue-pro 框架**（1天）
2. **架构师：设计完整数据库表结构**（2天）
3. **全员：学习 ruoyi 框架**（2天）
4. **Week 3 开始：正式开发**

---

## 签字确认

- [x] 架构师：________ （方案可行，代码生成是关键）
- [x] 高级开发：________ （分工明确，复杂逻辑有把握）
- [x] 初级开发A：________ （CRUD 有信心，学习 ruoyi）
- [x] 初级开发B：________ （前端页面有把握，占位符简单）

**一致通过 ✅**
