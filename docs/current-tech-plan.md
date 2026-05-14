# 当前技术方案

## 目标

用一个轻量、可维护、可扩展的标准 Web 架构，替代旧的 jeecg 脚手架方案。系统核心是 **Fact → Attribution → Metrics → Decision** 数据流闭环。

## 技术栈与选型理由

| 技术 | 用途 | 选型理由 |
|------|------|---------|
| Spring Boot 3.x | 后端框架 | 生态成熟、开箱即用、社区活跃 |
| JDK 21 | 运行时 | LTS 版本，支持虚拟线程、Pattern Matching |
| Maven | 构建 | 标准化依赖管理，CI/CD 友好 |
| MyBatis-Plus | 数据访问 | 在 MyBatis 之上提供 CRUD 增强，减少样板代码 |
| Lombok | 样板消除 | 自动生成 getter/setter/builder，减少 70% 实体代码 |
| Hutool | 工具库 | 国内最全的 Java 工具集，减少轮子 |
| Vue 3 | 前端框架 | Composition API、性能优越、TypeScript 友好 |
| Vue Router | 路由管理 | Vue 官方路由，支持嵌套/守卫/懒加载 |
| Element Plus | UI 组件 | 企业级组件库，表格/表单/树形控件完善 |
| MySQL 8.0 | 关系数据库 | 成熟稳定，财务场景需要 ACID 保证 |
| Redis 6.0 | 缓存 | Token 存储、会话管理、热点数据缓存 |
| Docker Compose | 本地编排 | 一键拉起 MySQL + Redis，统一开发环境 |

## 后端分层架构

```text
┌──────────────────────────────────────────────────────────┐
│                    Controller 层                          │
│  接收 HTTP 请求 → 参数校验 → 调用 Application → 返回 VO   │
│  不含任何业务逻辑                                         │
├──────────────────────────────────────────────────────────┤
│                   Application 层                          │
│  用例编排 → 事务管理 → 权限检查 → 调用 Domain/Repository  │
│  协调多个领域服务，但不承载领域规则                         │
├──────────────────────────────────────────────────────────┤
│                     Domain 层                             │
│  聚合根 → 实体 → 值对象 → 领域服务 → 仓储接口             │
│  所有业务规则和不变量都在此层实现                           │
├──────────────────────────────────────────────────────────┤
│                  Infrastructure 层                        │
│  Mapper → Repository 实现 → 外部服务适配                  │
│  将领域模型映射到数据库，实现仓储接口                      │
└──────────────────────────────────────────────────────────┘
```

### 各层职责与约束

| 层 | 包路径 | 职责 | 禁止 |
|----|--------|------|------|
| Controller | `com.profit.controller` | 参数接收、@Valid 校验、调用 AppService、包装 ApiResponse | 写业务逻辑、直接调用 Mapper |
| Application | `com.profit.application` | 用例编排、@Transactional、权限校验、DTO↔Domain 转换 | 包含领域规则、直接操作数据库 |
| Domain | `com.profit.domain` | 聚合根、实体、值对象、领域服务、Repository 接口 | 依赖 Spring 注解、依赖 Infrastructure |
| Infrastructure | `com.profit.infrastructure` | MyBatis-Plus Mapper、Repository 实现、外部 API 适配 | 包含业务逻辑 |
| DTO | `com.profit.dto` | 接口入参对象 | 包含业务方法 |
| VO | `com.profit.vo` | 接口出参对象 | 包含业务方法 |
| Common | `com.profit.common` | ApiResponse、异常处理、常量、工具 | 依赖业务模块 |

### 层间调用关系

```text
Controller ──→ Application ──→ Domain ──→ Repository(接口)
                  │                              ↑
                  │                    Infrastructure(实现)
                  └──→ Infrastructure(直接查询)
```

**依赖规则：**
- Domain 层不依赖任何外层，是系统的核心
- Application 依赖 Domain，不依赖 Infrastructure
- Controller 只依赖 Application 和 DTO/VO
- Infrastructure 实现 Domain 定义的 Repository 接口

### 包结构（完整）

```text
src/main/java/com/profit/
├── ProfitDecisionSystemApplication.java
├── auth/                        # 认证鉴权（独立模块，不走 DDD 分层）
│   ├── AuthController.java
│   ├── AuthService.java
│   ├── AuthInterceptor.java
│   ├── AuthTokenResolver.java
│   ├── TokenStore.java
│   ├── AuthSession.java
│   ├── AuthProperties.java
│   ├── AuthConstants.java
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   └── CurrentUser.java
├── dashboard/                   # 仪表盘（轻量查询模块）
│   ├── DashboardController.java
│   ├── DashboardSummary.java
│   └── DashboardMetric.java
├── controller/                  # REST 接口层
│   ├── HealthController.java
│   └── AccountSubjectController.java   # [待实现]
├── application/                 # 用例编排层
│   └── AccountSubjectAppService.java   # [待实现]
├── domain/                      # 领域层
│   └── accountsubject/
│       ├── AccountSubject.java          # [待实现] 聚合根
│       ├── AccountSubjectType.java      # [待实现] 值对象
│       ├── DebitCredit.java             # [待实现] 值对象
│       ├── AccountSubjectRepository.java # [待实现] 仓储接口
│       └── AccountSubjectDomainService.java # [待实现] 领域服务
├── infrastructure/              # 基础设施层
│   └── accountsubject/
│       ├── AccountSubjectMapper.java         # [待实现]
│       ├── AccountSubjectEntity.java         # [待实现] 持久化实体
│       └── AccountSubjectRepositoryImpl.java # [待实现]
├── dto/
│   └── AccountSubjectDTO.java   # [待实现]
├── vo/
│   └── AccountSubjectVO.java    # [待实现]
├── config/
│   └── WebConfig.java
├── common/
│   ├── api/
│   │   └── ApiResponse.java
│   └── exception/
│       ├── BusinessException.java
│       └── GlobalExceptionHandler.java
└── entity/                      # 通用数据实体（非 DDD 模块复用）
```

## 前端分层架构

```text
┌─────────────────────────────────────────────┐
│                 Views 层                     │
│     页面视图，组合组件和数据                   │
├─────────────────────────────────────────────┤
│              Components 层                   │
│     可复用 UI 组件，不含业务逻辑              │
├─────────────────────────────────────────────┤
│              Stores 层 (Pinia)              │
│     状态管理，持有页面/全局状态               │
├─────────────────────────────────────────────┤
│                API 层                        │
│     封装后端接口调用                          │
├─────────────────────────────────────────────┤
│            Router + Layouts                  │
│     路由守卫 + 页面布局框架                   │
└─────────────────────────────────────────────┘
```

### 前端包结构

```text
frontend/src/
├── api/
│   ├── auth.ts              # 认证接口
│   └── accountSubject.ts    # [待实现] 会计科目接口
├── views/
│   ├── LoginView.vue        # 登录页
│   ├── HomeView.vue         # 首页仪表盘
│   └── accountSubject/      # [待实现]
│       └── AccountSubjectView.vue
├── components/              # 公共组件
├── layouts/
│   └── MainLayout.vue       # 主布局（侧栏+头部+内容区）
├── router/
│   └── index.ts             # 路由配置
├── stores/                  # Pinia 状态管理
└── utils/                   # 工具函数
```

## API 设计规范

### 统一响应结构

```json
{
  "code": 0,
  "message": "ok",
  "data": { ... }
}
```

错误时：

```json
{
  "code": 40001,
  "message": "科目编码已存在",
  "data": null
}
```

### RESTful 约定

| 操作 | HTTP 方法 | URL 模式 | 说明 |
|------|-----------|---------|------|
| 查询列表/树 | GET | `/api/{resource}` | 返回列表或树形数据 |
| 查询详情 | GET | `/api/{resource}/{id}` | 返回单条数据 |
| 新增 | POST | `/api/{resource}` | 请求体为 DTO |
| 修改 | PUT | `/api/{resource}/{id}` | 请求体为 DTO |
| 删除 | DELETE | `/api/{resource}/{id}` | 软删除或逻辑删除 |
| 状态切换 | PATCH | `/api/{resource}/{id}/status` | 启用/停用 |

### 错误码规范

| 范围 | 含义 |
|------|------|
| 0 | 成功 |
| 400xx | 参数校验错误 |
| 401xx | 认证/授权错误 |
| 404xx | 资源不存在 |
| 409xx | 业务冲突（唯一键冲突、父子依赖等） |
| 500xx | 系统内部错误 |

### account_subject 接口契约

| 接口 | 方法 | URL | 入参 | 出参 |
|------|------|-----|------|------|
| 查询树 | GET | `/api/account-subject/tree` | `?keyword=xxx` | `ApiResponse<List<AccountSubjectVO>>` |
| 查询详情 | GET | `/api/account-subject/{id}` | - | `ApiResponse<AccountSubjectVO>` |
| 新增 | POST | `/api/account-subject` | `AccountSubjectDTO` | `ApiResponse<AccountSubjectVO>` |
| 修改 | PUT | `/api/account-subject/{id}` | `AccountSubjectDTO` | `ApiResponse<AccountSubjectVO>` |
| 删除 | DELETE | `/api/account-subject/{id}` | - | `ApiResponse<Void>` |
| 启停 | PATCH | `/api/account-subject/{id}/status` | `{ "enabled": true }` | `ApiResponse<Void>` |

## 数据库约定

- 本地开发使用 `docker-compose.yml` 中的 `profit-mysql`
- 数据库名：`profit`（新主线，不再使用 `jeecg-boot`）
- 字符集：`utf8mb4`，排序规则：`utf8mb4_unicode_ci`
- 主键使用 `BIGINT` 自增
- 时间字段使用 `DATETIME`，业务日期使用 `DATE`
- 金额字段使用 `DECIMAL(15,2)`
- 状态字段使用 `VARCHAR(20)`
- 每张表必须有 `created_at` 和 `updated_at`
- 逻辑删除优先于物理删除

## 实施路线

```text
Phase 1: 主数据模块                          ← 当前阶段
├── 会计科目（account_subject）建表 + 后端分层 + 前端页面
└── 往来方（counterparty）复用科目模式

Phase 2: 事实与账款
├── 收支记录（fact_event）
├── 应收账款（receivable）
└── 应付账款（payable）

Phase 3: 核算与预算
├── 项目核算（project）
├── 预算管理（budget + budget_adjustment）
└── 期间结账（period_closing）

Phase 4: 归因与指标
├── 归因计算（attribution）
├── 指标快照（metric_snapshot）
└── 经营看板增强

Phase 5: 决策与优化
├── 决策建议生成
├── 执行跟踪
└── 客户分析
```

## 当前优先级

1. **第一优先：** 会计科目模块全链路闭环（建表 → Domain → Application → Controller → 前端）
2. **第二优先：** 往来方模块复用同一套模式
3. **第三优先：** 前后端联调 + 测试 + 统一校验/异常使用示例
