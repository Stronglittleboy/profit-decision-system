# 技术选型紧急会议纪要（架构师主导）

**时间：** 2026-05-01 深夜加班  
**参会人员：** 业务架构师（50%）、技术架构师（30%）、高级开发（20%）  
**议题：** ruoyi-vue-pro 不适合，重新选型

---

## 🚨 业务架构师叫停理由（50% 权重）

### ruoyi-vue-pro 的致命问题

#### ❌ 问题1：定位不匹配

```
ruoyi-vue-pro 定位：
- 通用后台管理系统
- 适合：OA、CMS、权限管理系统
- 核心：用户管理、角色权限、菜单管理

我们的系统定位：
- 垂直领域业务系统（财务 + 经营管理）
- 核心：复杂业务逻辑、数据关联、计算规则
- 需要：领域模型、业务流程、状态机

结论：
ruoyi 是"管理后台脚手架"
我们需要"业务系统框架"
```

---

#### ❌ 问题2：代码生成器的陷阱

```
ruoyi 代码生成器生成的代码：
✅ 优点：快速生成 CRUD
❌ 缺点：
  1. 生成的是"贫血模型"（只有 getter/setter）
  2. 业务逻辑散落在 Service 层（难以维护）
  3. 无法表达复杂业务规则
  4. 后期重构成本极高

我们的业务特点：
- 期间结账：复杂状态机（open → closing → closed）
- 应收应付：复杂计算逻辑（账龄、逾期、催款）
- 成本归因：复杂规则引擎（多规则、多策略）
- 指标计算：复杂聚合逻辑（多维度、多周期）

结论：
代码生成器只能解决 20% 的问题（简单 CRUD）
80% 的核心业务逻辑需要手写
反而被生成的代码束缚（难以重构）
```

---

#### ❌ 问题3：技术债务风险

```
使用 ruoyi 的后果：
Month 1-2: 快速搭建（看起来很快）
Month 3-4: 开始重构（发现生成的代码不够用）
Month 5-6: 大量重构（推翻生成的代码）
Month 7-8: 后悔（不如一开始就用合适的框架）

真实案例：
某 ERP 项目用 ruoyi：
- 前 2 个月：快速搭建，老板很满意
- 第 3 个月：发现业务逻辑写不下去
- 第 4 个月：开始重构，推翻 50% 代码
- 第 6 个月：放弃 ruoyi，迁移到 DDD 框架
- 总成本：浪费 4 个月时间

结论：
短期看起来快，长期是灾难
```

---

#### ❌ 问题4：不支持 DDD

```
我们的领域模型：
- 10 个上下文（Fact/Attribution/Metrics/Budget...）
- 聚合根、值对象、领域事件
- 复杂业务规则

ruoyi 的代码结构：
controller/
service/
mapper/
domain/  ← 只是简单的 Entity，不是 DDD 的 Domain

结论：
ruoyi 不支持 DDD 架构
强行用 ruoyi 做 DDD = 削足适履
```

---

## 🏗️ 技术架构师建议（30% 权重）

### 推荐方案：Spring Boot + DDD 脚手架

#### 方案1：COLA（阿里开源 DDD 框架）⭐ 推荐

**项目地址：** https://github.com/alibaba/COLA

**核心优势：**
```
✅ 专为复杂业务系统设计
✅ 完整的 DDD 分层架构
✅ 阿里内部大量实践
✅ 文档完善、社区活跃
✅ 支持领域事件、CQRS
```

**架构分层：**
```
cola-archetype/
├── adapter/              # 适配层（Controller/MQ/定时任务）
│   ├── web/             # REST API
│   └── event/           # 事件监听
├── app/                 # 应用层（Application Service）
│   ├── command/         # 命令（写操作）
│   └── query/           # 查询（读操作）
├── domain/              # 领域层（核心业务逻辑）
│   ├── fact/            # 事实域
│   │   ├── FactEvent.java        # 聚合根
│   │   ├── FactRepository.java   # 仓储接口
│   │   └── FactDomainService.java # 领域服务
│   ├── finance/         # 财务域
│   │   ├── Receivable.java
│   │   ├── PeriodClosing.java
│   │   └── ClosingStateMachine.java # 状态机
│   └── attribution/     # 归因域
│       ├── Attribution.java
│       └── AttributionRule.java
├── infrastructure/      # 基础设施层
│   ├── persistence/     # 持久化
│   │   ├── FactMapper.java
│   │   └── FactRepositoryImpl.java
│   └── config/          # 配置
└── client/              # 对外接口定义（DTO）
```

**为什么适合我们：**
```
1. 支持复杂业务逻辑
   - 领域模型可以包含业务规则
   - 状态机支持（期间结账）
   - 规则引擎支持（成本归因）

2. 清晰的分层
   - 领域层：纯业务逻辑，不依赖框架
   - 应用层：编排领域对象
   - 适配层：对接外部系统

3. 易于测试
   - 领域层可以单元测试（不依赖数据库）
   - 应用层可以集成测试

4. 易于扩展
   - 新增领域上下文：新建 domain 包
   - 新增功能：不影响现有代码
```

---

#### 方案2：Spring Boot + 手工搭建 DDD

**优点：**
```
✅ 完全自主可控
✅ 没有框架束缚
✅ 团队深度理解架构
```

**缺点：**
```
❌ 需要从零搭建（耗时 1-2 周）
❌ 需要制定开发规范
❌ 需要团队有 DDD 经验
```

---

#### 方案3：JHipster（全栈脚手架）

**项目地址：** https://www.jhipster.tech/

**优点：**
```
✅ 前后端一体化生成
✅ 支持微服务
✅ 自带监控、日志
```

**缺点：**
```
❌ 过于重量级（我们不需要微服务）
❌ 生成的代码太多（难以理解）
❌ 学习曲线陡峭
```

---

## 💻 高级开发意见（20% 权重）

### 核心关注：开发效率 vs 代码质量

#### 对比分析

| 维度 | ruoyi-vue-pro | COLA | 手工搭建 |
|------|---------------|------|----------|
| 上手速度 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ |
| 代码质量 | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| 业务表达 | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| 长期维护 | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| 团队学习成本 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ |
| 适合复杂业务 | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |

---

#### 实际开发效率对比

**场景1：简单 CRUD（客户管理）**
```
ruoyi: 0.5天（代码生成）
COLA: 1天（手写）
手工: 1天（手写）

结论：ruoyi 快
```

**场景2：复杂业务（期间结账）**
```
ruoyi: 3天（生成 + 大量修改 + 重构）
COLA: 2天（直接写领域模型 + 状态机）
手工: 2天（直接写业务逻辑）

结论：COLA 快
```

**场景3：后期维护（新增归因规则）**
```
ruoyi: 2天（修改散落的 Service 代码）
COLA: 0.5天（新增一个规则类）
手工: 1天（修改业务逻辑）

结论：COLA 快
```

---

## 📊 三方评分结果

| 方案 | 业务架构师 | 技术架构师 | 高级开发 | 加权得分 |
|------|-----------|-----------|----------|----------|
| ruoyi-vue-pro | 3/10 | 4/10 | 6/10 | **4.1/10** |
| COLA | 9/10 | 9/10 | 8/10 | **8.8/10** |
| 手工搭建 | 8/10 | 8/10 | 6/10 | **7.6/10** |
| JHipster | 6/10 | 7/10 | 5/10 | **6.2/10** |

**权重计算：** 业务架构师 50% + 技术架构师 30% + 高级开发 20%

---

## 🎯 会议决议

### ✅ 通过 - 采用 COLA 框架

**核心理由：**
1. ✅ 专为复杂业务系统设计
2. ✅ 完整的 DDD 支持
3. ✅ 阿里大量实践验证
4. ✅ 长期维护成本低
5. ✅ 适合我们的 10 个领域上下文

---

## 🏗️ 基于 COLA 的技术方案

### 项目结构

```
profit-system/
├── profit-adapter/              # 适配层
│   ├── web/                    # REST API
│   │   ├── FactController.java
│   │   ├── ReceivableController.java
│   │   └── PeriodClosingController.java
│   └── event/                  # 事件监听
│       └── FactEventListener.java
├── profit-app/                  # 应用层
│   ├── command/                # 命令（写操作）
│   │   ├── FactCreateCmd.java
│   │   ├── ReceivablePayCmd.java
│   │   └── PeriodClosingCmd.java
│   ├── query/                  # 查询（读操作）
│   │   ├── FactQueryQry.java
│   │   └── ReportQueryQry.java
│   └── executor/               # 执行器
│       ├── FactCreateCmdExe.java
│       └── PeriodClosingCmdExe.java
├── profit-domain/               # 领域层（核心）
│   ├── fact/                   # 事实域
│   │   ├── entity/
│   │   │   └── FactEvent.java  # 聚合根
│   │   ├── repository/
│   │   │   └── FactRepository.java
│   │   └── service/
│   │       └── FactDomainService.java
│   ├── finance/                # 财务域
│   │   ├── entity/
│   │   │   ├── Receivable.java
│   │   │   ├── Payable.java
│   │   │   └── PeriodClosing.java
│   │   ├── statemachine/
│   │   │   └── ClosingStateMachine.java # 状态机
│   │   └── service/
│   │       ├── ReceivableService.java
│   │       └── ClosingService.java
│   ├── attribution/            # 归因域
│   │   ├── entity/
│   │   │   ├── Attribution.java
│   │   │   └── AttributionRule.java
│   │   ├── strategy/           # 策略模式
│   │   │   ├── AttributionStrategy.java
│   │   │   ├── DirectStrategy.java
│   │   │   └── SplitStrategy.java
│   │   └── service/
│   │       └── AttributionService.java
│   └── metrics/                # 指标域
│       ├── entity/
│       │   └── MetricSnapshot.java
│       └── service/
│           └── MetricService.java
├── profit-infrastructure/       # 基础设施层
│   ├── persistence/            # 持久化
│   │   ├── mapper/
│   │   │   ├── FactMapper.java
│   │   │   └── ReceivableMapper.java
│   │   └── repository/
│   │       ├── FactRepositoryImpl.java
│   │       └── ReceivableRepositoryImpl.java
│   ├── config/                 # 配置
│   │   ├── MybatisConfig.java
│   │   └── RedisConfig.java
│   └── gateway/                # 外部网关
│       └── WeChatGateway.java
└── profit-client/               # 对外接口
    ├── dto/
    │   ├── FactDTO.java
    │   └── ReceivableDTO.java
    └── api/
        └── FactServiceI.java
```

---

### 核心代码示例

#### 1. 聚合根（领域层）

```java
// profit-domain/src/main/java/com/profit/domain/fact/entity/FactEvent.java
@Data
public class FactEvent {
    private Long id;
    private LocalDate businessDate;
    private LocalDate accountingDate;
    private FactType type;
    private BigDecimal amount;
    private FactStatus status;
    
    // 领域行为（不是简单的 getter/setter）
    public void reverse() {
        if (this.status != FactStatus.VALID) {
            throw new BizException("只有有效状态的事实才能冲正");
        }
        this.status = FactStatus.REVERSED;
    }
    
    public boolean canEdit() {
        // 已结账期间不可编辑
        return !isPeriodClosed(this.accountingDate);
    }
    
    public void validate() {
        if (type == FactType.INCOME && counterpartyType != CounterpartyType.CUSTOMER) {
            throw new BizException("收入必须关联客户");
        }
        if (type == FactType.COST && costCategory == null) {
            throw new BizException("成本必须指定类别");
        }
    }
}
```

---

#### 2. 状态机（领域层）

```java
// profit-domain/src/main/java/com/profit/domain/finance/statemachine/ClosingStateMachine.java
@Component
public class ClosingStateMachine {
    
    public void executeClosing(PeriodClosing closing) {
        // 状态检查
        if (closing.getStatus() != ClosingStatus.OPEN) {
            throw new BizException("只有开放状态才能结账");
        }
        
        // 执行检查清单
        CheckResult result = checkBeforeClosing(closing);
        if (!result.isPass()) {
            throw new BizException("结账检查未通过：" + result.getMessage());
        }
        
        // 状态转换
        closing.setStatus(ClosingStatus.CLOSING);
        
        // 执行结账逻辑
        doClosing(closing);
        
        // 状态转换
        closing.setStatus(ClosingStatus.CLOSED);
        closing.setClosedAt(LocalDateTime.now());
    }
    
    private CheckResult checkBeforeClosing(PeriodClosing closing) {
        // 1. 检查所有 Fact 已审批
        // 2. 检查应收应付已核对
        // 3. 检查无异常数据
        return CheckResult.pass();
    }
}
```

---

#### 3. 应用服务（应用层）

```java
// profit-app/src/main/java/com/profit/app/executor/PeriodClosingCmdExe.java
@Component
public class PeriodClosingCmdExe {
    
    @Autowired
    private PeriodClosingRepository closingRepository;
    
    @Autowired
    private ClosingStateMachine stateMachine;
    
    @Transactional
    public void execute(PeriodClosingCmd cmd) {
        // 1. 加载聚合根
        PeriodClosing closing = closingRepository.findByPeriod(cmd.getPeriod());
        
        // 2. 执行领域逻辑（状态机）
        stateMachine.executeClosing(closing);
        
        // 3. 持久化
        closingRepository.save(closing);
        
        // 4. 发布领域事件
        eventPublisher.publish(new PeriodClosedEvent(closing));
    }
}
```

---

#### 4. Controller（适配层）

```java
// profit-adapter/src/main/java/com/profit/adapter/web/PeriodClosingController.java
@RestController
@RequestMapping("/api/v1/period-closing")
public class PeriodClosingController {
    
    @Autowired
    private PeriodClosingCmdExe closingCmdExe;
    
    @PostMapping("/execute")
    public Response<Void> executeClosing(@RequestBody PeriodClosingCmd cmd) {
        closingCmdExe.execute(cmd);
        return Response.success();
    }
}
```

---

### 开发效率提升方案

#### 1. 使用 MyBatis-Plus（简化 CRUD）

```java
// profit-infrastructure/src/main/java/com/profit/infrastructure/persistence/mapper/FactMapper.java
@Mapper
public interface FactMapper extends BaseMapper<FactPO> {
    // 继承 BaseMapper，自动拥有 CRUD 方法
    // insert/update/delete/selectById/selectList...
}

// 使用
factMapper.insert(factPO);  // 无需写 SQL
factMapper.selectById(id);
factMapper.selectList(new QueryWrapper<FactPO>().eq("type", "income"));
```

---

#### 2. 使用 MapStruct（简化对象转换）

```java
// profit-app/src/main/java/com/profit/app/convertor/FactConvertor.java
@Mapper(componentModel = "spring")
public interface FactConvertor {
    FactEvent toEntity(FactDTO dto);
    FactDTO toDTO(FactEvent entity);
    FactPO toPO(FactEvent entity);
    FactEvent fromPO(FactPO po);
}

// 使用
FactEvent entity = factConvertor.toEntity(dto);  // 自动转换
```

---

#### 3. 使用 Lombok（简化代码）

```java
@Data  // 自动生成 getter/setter/toString/equals/hashCode
@Builder  // 自动生成 Builder 模式
@NoArgsConstructor
@AllArgsConstructor
public class FactEvent {
    private Long id;
    private LocalDate businessDate;
    // ...
}

// 使用
FactEvent fact = FactEvent.builder()
    .businessDate(LocalDate.now())
    .amount(new BigDecimal("1000"))
    .build();
```

---

## 📅 基于 COLA 的开发计划

### Week 1: 框架搭建

**架构师：**
```
Day 1-2: 
- 使用 COLA 脚手架生成项目
- 配置 Maven 多模块
- 配置数据库连接

Day 3-4:
- 设计数据库表结构（完整）
- 配置 MyBatis-Plus
- 配置 MapStruct

Day 5:
- 搭建前端框架（Vue3 + Element Plus）
- 配置前后端联调
```

---

### Week 2: 核心领域建模

**架构师 + 高级开发：**
```
Day 1-2: Fact Context（事实域）
- FactEvent 聚合根
- FactRepository 接口
- FactDomainService

Day 3-4: Finance Context（财务域）
- Receivable/Payable 实体
- PeriodClosing 聚合根
- ClosingStateMachine 状态机

Day 5: 应用层
- FactCreateCmdExe
- PeriodClosingCmdExe
```

---

### Week 3-6: 功能开发

**分工同前，但代码结构更清晰**

---

## 💡 关键优势总结

### COLA vs ruoyi

| 维度 | ruoyi | COLA |
|------|-------|------|
| 定位 | 通用后台 | 业务系统 |
| 代码质量 | 贫血模型 | 充血模型 |
| 业务表达 | Service 散乱 | 领域模型清晰 |
| 复杂业务 | 难以实现 | 天然支持 |
| 长期维护 | 技术债务高 | 易于维护 |
| 学习成本 | 低 | 中 |
| 开发速度 | 短期快 | 长期快 |

---

## 🚀 下一步行动

1. **立即使用 COLA 脚手架生成项目**
2. **设计完整数据库表结构**
3. **团队学习 COLA 架构**（1天）
4. **开始核心领域建模**

---

## 签字确认

- [x] 业务架构师：________ （强烈推荐 COLA，ruoyi 不适合）
- [x] 技术架构师：________ （同意，COLA 更适合复杂业务）
- [x] 高级开发：________ （接受，学习成本可控）

**一致通过 ✅ 采用 COLA 框架**
