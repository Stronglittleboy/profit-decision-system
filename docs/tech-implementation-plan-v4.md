# 技术部门三次方案分析会议 - 落地实施与质量把控

**会议主题：** v4.0 技术落地方案、质量把控、任务拆分  
**会议时间：** 2026-05-04 20:40  
**参会人员：** 架构师、高级开发、初级开发A、初级开发B、DBA、测试工程师  
**会议目标：** 制定可执行的开发计划，确保质量和进度

---

## 📋 会议议程

1. **架构师：** 技术架构设计与模块划分
2. **高级开发：** 核心模块实现方案
3. **DBA：** 数据库实施方案
4. **初级开发A：** 后端任务拆分
5. **初级开发B：** 前端任务拆分
6. **测试工程师：** 测试策略与质量把控

---

## 🎭 第一部分：架构师 - 技术架构设计

**发言人：** 赵架构  
**主题：** 模块划分、技术选型、接口设计

### 1. 模块划分（DDD 分层）

#### 1.1 领域层划分

```
profit-decision-system/
├── domain/
│   ├── fact/              # 事实域
│   │   ├── FactEvent.java
│   │   └── FactEventService.java
│   ├── accounting/        # 会计域 🆕
│   │   ├── AccountSubject.java
│   │   ├── Voucher.java
│   │   ├── VoucherEntry.java
│   │   └── VoucherService.java
│   ├── attribution/       # 归因域
│   │   ├── Attribution.java
│   │   └── AttributionService.java
│   ├── metrics/           # 指标域
│   │   ├── MetricSnapshot.java
│   │   └── MetricsService.java
│   ├── budget/            # 预算域
│   │   ├── Budget.java
│   │   └── BudgetService.java
│   └── masterdata/        # 主数据域
│       ├── Counterparty.java
│       ├── Project.java
│       └── OrgUnit.java
```

#### 1.2 应用层划分

```
application/
├── fact/
│   ├── FactEventAppService.java
│   └── dto/
│       ├── FactEventCreateDTO.java
│       └── FactEventVO.java
├── accounting/
│   ├── VoucherAppService.java
│   └── dto/
│       ├── VoucherCreateDTO.java
│       └── VoucherVO.java
└── ...
```

### 2. 核心接口设计

#### 2.1 收支管理接口

```java
@RestController
@RequestMapping("/api/fact-event")
public class FactEventController {
    
    /**
     * 创建收支记录
     * 自动生成凭证
     */
    @PostMapping
    public Result<FactEventVO> create(@RequestBody FactEventCreateDTO dto) {
        // 1. 创建 fact_event
        FactEvent factEvent = factEventService.create(dto);
        
        // 2. 自动生成凭证
        Voucher voucher = voucherService.generateFromFactEvent(factEvent);
        
        // 3. 返回结果
        return Result.ok(FactEventVO.from(factEvent, voucher));
    }
    
    /**
     * 查询收支列表
     */
    @GetMapping
    public Result<Page<FactEventVO>> list(FactEventQueryDTO query) {
        return Result.ok(factEventService.list(query));
    }
}
```

#### 2.2 凭证管理接口

```java
@RestController
@RequestMapping("/api/voucher")
public class VoucherController {
    
    /**
     * 查询凭证详情（含分录）
     */
    @GetMapping("/{id}")
    public Result<VoucherVO> get(@PathVariable Long id) {
        Voucher voucher = voucherService.getById(id);
        List<VoucherEntry> entries = voucherEntryService.listByVoucherId(id);
        return Result.ok(VoucherVO.from(voucher, entries));
    }
    
    /**
     * 审核凭证
     */
    @PostMapping("/{id}/review")
    public Result<Void> review(@PathVariable Long id) {
        voucherService.review(id);
        return Result.ok();
    }
}
```

### 3. 技术选型确认

| 技术栈 | 选型 | 版本 | 说明 |
|--------|------|------|------|
| 后端框架 | Spring Boot | 3.5.5 | jeecg-boot 基础 |
| ORM | MyBatis-Plus | 3.5.12 | 代码生成 |
| 数据库 | MySQL | 8.0 | 已确认 |
| 缓存 | Redis | 6.0 | 已确认 |
| 前端框架 | Vue 3 | 3.5.27 | jeecg-boot 基础 |
| UI 组件 | Ant Design Vue | 4.0 | jeecg-boot 基础 |

### 4. 关键技术决策

#### 决策 1：fact_event 和 voucher 的关系

**方案：** 松耦合，通过 voucher_entry.fact_event_id 关联

**理由：**
- 一个 fact_event 可能对应多个 voucher_entry
- 支持复杂的会计分录（一借多贷、多借多贷）
- 业务和会计分离

#### 决策 2：凭证自动生成策略

**方案：** 策略模式 + 模板方法

```java
public interface VoucherGenerateStrategy {
    List<VoucherEntry> generate(FactEvent factEvent);
}

public class IncomeVoucherStrategy implements VoucherGenerateStrategy {
    @Override
    public List<VoucherEntry> generate(FactEvent factEvent) {
        // 收入：借银行存款，贷主营业务收入
        return Arrays.asList(
            createDebitEntry("1002", factEvent.getAmount()),
            createCreditEntry("6001", factEvent.getAmount())
        );
    }
}
```

#### 决策 3：审批流程实现

**方案：** 状态机模式

```java
public enum ApprovalStatus {
    DRAFT("草稿"),
    PENDING("待审批"),
    REVIEWING("审核中"),
    APPROVED("已批准"),
    REJECTED("已拒绝");
}

public class ApprovalStateMachine {
    public void submit(ApprovalFlow flow) {
        if (flow.getStatus() == DRAFT) {
            flow.setStatus(PENDING);
            flow.setCurrentStep(1);
        }
    }
    
    public void approve(ApprovalFlow flow) {
        if (flow.getCurrentStep() < flow.getTotalSteps()) {
            flow.setCurrentStep(flow.getCurrentStep() + 1);
        } else {
            flow.setStatus(APPROVED);
        }
    }
}
```

---

## 🎭 第二部分：高级开发 - 核心模块实现

**发言人：** 王高工  
**主题：** 核心业务逻辑实现方案

### 1. voucher 自动生成逻辑

#### 1.1 整体流程

```
用户录入收支
    ↓
创建 FactEvent
    ↓
触发凭证生成事件
    ↓
根据业务类型选择策略
    ↓
生成 Voucher + VoucherEntry
    ↓
检查借贷平衡
    ↓
保存凭证
```

#### 1.2 代码实现

```java
@Service
public class VoucherService {
    
    @Autowired
    private Map<String, VoucherGenerateStrategy> strategyMap;
    
    @Transactional
    public Voucher generateFromFactEvent(FactEvent factEvent) {
        // 1. 创建凭证头
        Voucher voucher = new Voucher();
        voucher.setVoucherNo(generateVoucherNo());
        voucher.setVoucherDate(factEvent.getAccountingDate());
        voucher.setPeriod(getPeriod(factEvent.getAccountingDate()));
        voucher.setVoucherType(getVoucherType(factEvent.getType()));
        voucher.setPreparedBy(getCurrentUserId());
        voucher.setStatus(VoucherStatus.DRAFT);
        
        voucherMapper.insert(voucher);
        
        // 2. 根据策略生成凭证分录
        String strategyKey = factEvent.getType();
        VoucherGenerateStrategy strategy = strategyMap.get(strategyKey);
        List<VoucherEntry> entries = strategy.generate(factEvent);
        
        // 3. 设置凭证ID和关联
        entries.forEach(entry -> {
            entry.setVoucherId(voucher.getId());
            entry.setFactEventId(factEvent.getId());
        });
        
        voucherEntryMapper.insertBatch(entries);
        
        // 4. 计算借贷合计
        BigDecimal totalDebit = entries.stream()
            .map(VoucherEntry::getDebitAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCredit = entries.stream()
            .map(VoucherEntry::getCreditAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        voucher.setTotalDebit(totalDebit);
        voucher.setTotalCredit(totalCredit);
        
        // 5. 检查借贷平衡
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new BusinessException("借贷不平衡");
        }
        
        voucherMapper.updateById(voucher);
        
        // 6. 更新 fact_event 的凭证号
        factEvent.setVoucherNo(voucher.getVoucherNo());
        factEventMapper.updateById(factEvent);
        
        return voucher;
    }
    
    /**
     * 生成凭证号
     * 格式：记-202605-0001
     */
    private String generateVoucherNo() {
        String period = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        Integer maxNo = voucherMapper.getMaxNoByPeriod(period);
        int nextNo = (maxNo == null ? 0 : maxNo) + 1;
        return String.format("记-%s-%04d", period, nextNo);
    }
}
```

### 2. 预算执行进度自动更新

```java
@Service
public class BudgetService {
    
    /**
     * 更新预算执行进度
     * 在 fact_event 创建后触发
     */
    @Transactional
    public void updateProgress(Long budgetId) {
        Budget budget = budgetMapper.selectById(budgetId);
        
        // 计算已用金额
        BigDecimal usedAmount = factEventMapper.sumAmountByBudget(
            budget.getOrgUnitId(),
            budget.getPeriod(),
            budget.getCategory()
        );
        
        // 更新预算
        budget.setUsedAmount(usedAmount);
        budget.setRemainingAmount(
            budget.getBudgetedAmount().subtract(usedAmount)
        );
        
        budgetMapper.updateById(budget);
        
        // 预算超支预警
        if (budget.getRemainingAmount().compareTo(BigDecimal.ZERO) < 0) {
            sendBudgetAlert(budget);
        }
    }
}
```

### 3. 审批流程通用化

```java
@Service
public class ApprovalFlowService {
    
    /**
     * 提交审批
     */
    @Transactional
    public void submit(String businessType, Long businessId) {
        // 1. 创建审批流程
        ApprovalFlow flow = new ApprovalFlow();
        flow.setBusinessType(businessType);
        flow.setBusinessId(businessId);
        flow.setCurrentStep(1);
        flow.setTotalSteps(getApprovalSteps(businessType));
        flow.setStatus(ApprovalStatus.PENDING);
        
        approvalFlowMapper.insert(flow);
        
        // 2. 更新业务状态
        updateBusinessStatus(businessType, businessId, "pending_approval");
        
        // 3. 通知审批人
        notifyApprover(flow);
    }
    
    /**
     * 审批通过
     */
    @Transactional
    public void approve(Long flowId, Long approverId) {
        ApprovalFlow flow = approvalFlowMapper.selectById(flowId);
        
        if (flow.getCurrentStep() < flow.getTotalSteps()) {
            // 进入下一步
            flow.setCurrentStep(flow.getCurrentStep() + 1);
            approvalFlowMapper.updateById(flow);
            notifyApprover(flow);
        } else {
            // 审批完成
            flow.setStatus(ApprovalStatus.APPROVED);
            approvalFlowMapper.updateById(flow);
            updateBusinessStatus(flow.getBusinessType(), flow.getBusinessId(), "approved");
        }
        
        // 记录审批日志
        saveApprovalLog(flowId, approverId, "approve");
    }
}
```

---

## 🎭 第三部分：DBA - 数据库实施方案

**发言人：** 李DBA  
**主题：** 数据库创建、索引优化、性能调优

### 1. 数据库实施步骤

#### 步骤 1：备份现有数据

```bash
# 备份当前数据库
docker exec profit-mysql mysqldump -uroot -p123456 jeecg-boot > backup_$(date +%Y%m%d_%H%M%S).sql
```

#### 步骤 2：创建新表

```bash
# 执行 v4.0 表结构
docker exec -i profit-mysql mysql -uroot -p123456 jeecg-boot < schema-v4-reviewed.sql
```

#### 步骤 3：验证表结构

```sql
-- 验证表是否创建成功
SELECT table_name, table_rows 
FROM information_schema.tables 
WHERE table_schema = 'jeecg-boot' 
AND table_name LIKE 'fact_%' OR table_name LIKE 'voucher%';
```

### 2. 索引优化方案

#### 2.1 高频查询索引

```sql
-- fact_event 表
CREATE INDEX idx_accounting_date ON fact_event(accounting_date);
CREATE INDEX idx_org_unit_period ON fact_event(org_unit_id, accounting_date);
CREATE INDEX idx_counterparty ON fact_event(counterparty_type, counterparty_id);

-- voucher 表
CREATE INDEX idx_period_status ON voucher(period, status);
CREATE INDEX idx_voucher_date ON voucher(voucher_date);

-- voucher_entry 表
CREATE INDEX idx_voucher_id ON voucher_entry(voucher_id);
CREATE INDEX idx_account_subject ON voucher_entry(account_subject_id);
```

#### 2.2 分区表设计

```sql
-- audit_log 表按月分区
ALTER TABLE audit_log PARTITION BY RANGE (YEAR(created_at) * 100 + MONTH(created_at)) (
    PARTITION p202605 VALUES LESS THAN (202606),
    PARTITION p202606 VALUES LESS THAN (202607),
    PARTITION p202607 VALUES LESS THAN (202608),
    PARTITION p202608 VALUES LESS THAN (202609),
    PARTITION p202609 VALUES LESS THAN (202610),
    PARTITION p202610 VALUES LESS THAN (202611),
    PARTITION p202611 VALUES LESS THAN (202612),
    PARTITION p202612 VALUES LESS THAN (202701),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);
```

### 3. 性能监控

#### 3.1 慢查询监控

```sql
-- 开启慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;

-- 查看慢查询
SELECT * FROM mysql.slow_log ORDER BY start_time DESC LIMIT 10;
```

#### 3.2 索引使用情况

```sql
-- 查看未使用的索引
SELECT * FROM sys.schema_unused_indexes WHERE object_schema = 'jeecg-boot';

-- 查看索引效率
SELECT * FROM sys.schema_index_statistics WHERE table_schema = 'jeecg-boot';
```

---

## 🎭 第四部分：初级开发A - 后端任务拆分

**发言人：** 小张（后端）  
**主题：** 后端开发任务详细拆分

### 后端任务清单（19个模块，84小时）

| 模块 | 负责人 | 时间 | 优先级 | 依赖 |
|------|--------|------|--------|------|
| account_subject | 初级A | 1.5h | P0 | 无 |
| counterparty | 初级A | 1.5h | P0 | 无 |
| project | 初级A | 1h | P0 | 无 |
| org_unit | 初级A | 1h | P0 | 无 |
| voucher | 高级 | 2h | P0 | account_subject |
| voucher_entry | 高级 | 1h | P0 | voucher |
| voucher_generate | 高级 | 4h | P0 | voucher |
| fact_event | 初级A | 2h | P0 | account_subject |
| fact_event_service | 高级 | 2h | P0 | fact_event, voucher |
| fact_template | 初级A | 1.5h | P1 | fact_event |
| budget | 初级A | 1.5h | P1 | 无 |
| approval_flow | 高级 | 3h | P1 | 无 |
| audit_log | 初级A | 1h | P1 | 无 |
| attribution | 初级A | 1h | P2 | fact_event |
| metric_snapshot | 初级A | 1h | P2 | attribution |
| goal | 初级A | 0.5h | P2 | 无 |
| action_record | 初级A | 0.5h | P2 | 无 |
| budget_adjustment | 初级A | 0.5h | P2 | budget |
| user | 初级A | 0.5h | P2 | 无 |

---

## 🎭 第五部分：初级开发B - 前端任务拆分

**发言人：** 小李（前端）  
**主题：** 前端开发任务详细拆分

### 前端任务清单（19个页面，48小时）

| 页面 | 负责人 | 时间 | 优先级 | 依赖 |
|------|--------|------|--------|------|
| 会计科目管理 | 初级B | 2h | P0 | 后端 account_subject |
| 客户供应商管理 | 初级B | 2h | P0 | 后端 counterparty |
| 项目管理 | 初级B | 2h | P0 | 后端 project |
| 凭证列表 | 初级B | 2h | P0 | 后端 voucher |
| 凭证详情 | 初级B | 3h | P0 | 后端 voucher |
| 收支列表 | 初级B | 2h | P0 | 后端 fact_event |
| 收支快速录入 | 初级B | 2h | P0 | 后端 fact_event |
| 收支完整录入 | 初级B | 2h | P0 | 后端 fact_event |
| 收支模板管理 | 初级B | 2h | P1 | 后端 fact_template |
| 预算管理 | 初级B | 2h | P1 | 后端 budget |
| 审批流程 | 初级B | 2h | P1 | 后端 approval_flow |
| 审计日志 | 初级B | 1h | P1 | 后端 audit_log |

---

## 🎭 第六部分：测试工程师 - 质量把控

**发言人：** 赵测试  
**主题：** 测试策略与质量保证

### 测试计划（215个用例，24小时）

#### 1. 单元测试（120个用例，12小时）
- 凭证自动生成：30个用例
- 借贷平衡检查：20个用例
- 预算执行进度：15个用例
- 审批流程：25个用例
- 其他模块：30个用例

#### 2. 集成测试（60个用例，8小时）
- 收支→凭证→预算：15个用例
- 审批流程：15个用例
- 模板功能：10个用例
- 其他场景：20个用例

#### 3. E2E测试（35个用例，4小时）
- 完整业务流程：10个用例
- 异常场景：15个用例
- 性能测试：10个用例

---

## 📊 5天开发计划

### Day 1：主数据 + 会计基础（20小时）

**上午（10小时）：**
- 架构师：技术架构设计（2h）
- DBA：创建数据库表（2h）
- 高级开发：voucher 实体和 Mapper（2h）
- 初级A：account_subject + counterparty（3h）
- 初级B：会计科目页面 + 客户供应商页面（4h）
- 测试：编写测试用例（2h）

**下午（10小时）：**
- 架构师：代码审查（1h）
- 高级开发：凭证自动生成策略（4h）
- 初级A：project + org_unit（2h）
- 初级B：项目管理页面（2h）
- 测试：单元测试（2h）

**交付物：**
- ✅ 4个主数据模块（后端+前端）
- ✅ 凭证实体和自动生成策略
- ✅ 单元测试覆盖率 > 70%

---

### Day 2：凭证管理 + 收支管理（22小时）

**上午（11小时）：**
- 架构师：接口设计评审（1h）
- 高级开发：凭证审批流程（2h）+ fact_event_service（2h）
- 初级A：fact_event 实体（2h）
- 初级B：凭证列表 + 凭证详情（5h）
- DBA：索引优化（1h）
- 测试：集成测试（2h）

**下午（11小时）：**
- 高级开发：审批流程通用化（3h）
- 初级A：fact_template（1.5h）
- 初级B：收支列表 + 收支录入（4h）
- 测试：集成测试（2h）

**交付物：**
- ✅ 凭证管理完整功能
- ✅ 收支管理基础功能
- ✅ 集成测试通过

---

### Day 3：辅助功能（18小时）

**上午（9小时）：**
- 架构师：代码审查（1h）
- 高级开发：审计日志 AOP（1h）
- 初级A：budget + audit_log（2.5h）
- 初级B：收支模板 + 预算管理（4h）
- 测试：功能测试（2h）

**下午（9小时）：**
- 高级开发：性能优化（2h）
- 初级A：attribution + metric_snapshot（2h）
- 初级B：审批流程页面 + 审计日志页面（3h）
- DBA：分区表实施（1h）
- 测试：性能测试（2h）

**交付物：**
- ✅ 所有辅助功能完成
- ✅ 性能优化完成
- ✅ 功能测试通过

---

### Day 4：完善和测试（14小时）

**上午（7小时）：**
- 架构师：系统集成测试（1h）
- 高级开发：Bug 修复（2h）
- 初级A：剩余模块（goal, action_record等）（2h）
- 初级B：UI 优化（2h）
- 测试：E2E 测试（4h）

**下午（7小时）：**
- 全员：Bug 修复（4h）
- 测试：回归测试（3h）

**交付物：**
- ✅ 所有模块完成
- ✅ E2E 测试通过
- ✅ Bug 修复完成

---

### Day 5：上线准备（10小时）

**上午（5小时）：**
- 架构师：部署方案（1h）
- DBA：数据库备份和优化（1h）
- 全员：最终测试（3h）

**下午（5小时）：**
- 全员：文档编写（2h）
- 全员：上线演练（2h）
- 架构师：上线总结（1h）

**交付物：**
- ✅ 系统上线
- ✅ 文档完整
- ✅ 培训完成

---

## ✅ 质量保证措施

### 1. 代码质量

- **代码审查：** 所有代码必须经过高级开发审查
- **单元测试：** 核心模块覆盖率 ≥ 80%
- **代码规范：** 使用 ESLint + Prettier
- **静态分析：** SonarQube 扫描

### 2. 测试质量

- **测试覆盖：** 215个测试用例
- **自动化测试：** 单元测试 + 集成测试自动化
- **性能测试：** 响应时间 < 500ms
- **压力测试：** 并发 100 用户

### 3. 文档质量

- **API 文档：** Swagger 自动生成
- **开发文档：** README + 架构文档
- **用户手册：** 操作指南
- **测试报告：** 测试结果和覆盖率

---

## 📝 会议决议

### ✅ 批准实施

**v4.0 技术落地方案正式批准，明天（Day 1）开始实施。**

### 📋 行动计划

1. **今晚（2026-05-04）：**
   - DBA 更新数据库表结构
   - 架构师准备技术架构文档
   - 初级开发学习复式记账原理（4h）

2. **明天（Day 1）：**
   - 09:00 晨会（15分钟）
   - 09:15 开始开发
   - 12:00 午餐
   - 13:00 继续开发
   - 17:00 每日站会（15分钟）
   - 17:15 代码审查

3. **质量保证：**
   - 每日代码审查
   - 每日集成测试
   - 每日站会同步进度

---

**会议主持人：** 旺仔助手（技术总监）  
**会议时间：** 2026-05-04 20:40 - 21:30  
**会议状态：** ✅ 完成，批准实施  
**下一步：** 立即更新数据库，明天开始开发
