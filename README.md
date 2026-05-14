# 飞牛经营系统（Profit Decision System）

## 业务背景

企业经营管理中普遍存在以下痛点：

- **钱花在哪不清楚**：成本分散在多个部门、项目和供应商，缺乏统一归因
- **赚在哪不清楚**：收入贡献无法按客户、项目、部门精确核算
- **决策靠经验**：没有数据驱动的经营建议，预算执行缺少监控
- **科目/往来方等基础数据混乱**：会计科目、客户供应商等主数据缺少规范管理

本系统以 **Fact → Attribution → Metrics → Decision** 为核心数据流，构建一个从经营事实录入、成本归因、指标计算到决策建议的闭环系统。

## 系统全景

```text
┌─────────────────────────────────────────────────────────────────┐
│                        产品层 (Product)                         │
│              Goal(目标) / Status(现状) / Action(行动)            │
├─────────────────────────────────────────────────────────────────┤
│                       决策层 (Decision)                         │
│          问题识别 → 根因分析 → 建议生成 → 执行跟踪               │
├─────────────────────────────────────────────────────────────────┤
│                       指标层 (Metrics)                          │
│      收入/成本/利润/ROI/成本结构 · 按周期快照 · 支持重算          │
├─────────────────────────────────────────────────────────────────┤
│                       归因层 (Attribution)                      │
│      按规则将事实分摊到组织/项目/客户 · 支持批量重算              │
├─────────────────────────────────────────────────────────────────┤
│                       事实层 (Fact)                              │
│              收入事实 / 成本事实 / 唯一事实源                     │
├─────────────────────────────────────────────────────────────────┤
│                      主数据层 (Master Data)                      │
│     会计科目 / 往来方 / 组织单元 / 项目 / 合同 / 预算             │
└─────────────────────────────────────────────────────────────────┘
```

## 业务模块

| 模块 | 说明 | 当前状态 |
|------|------|----------|
| **会计科目** (account_subject) | 树形科目管理，财务核算的基础编码体系 | 领域设计+页面DSL已完成，待编码 |
| **往来方** (counterparty) | 客户/供应商管理，收支事实的交易对手 | 复用科目模式，待启动 |
| **收支记录** (fact_event) | 收入确认、成本录入，系统唯一事实源 | 数据库设计已完成 |
| **应收应付** (receivable/payable) | 账款管理与资金追踪 | 数据库设计已完成 |
| **项目核算** (project) | 项目维度的成本/收入/利润追踪 | 数据库设计已完成 |
| **预算管理** (budget) | 预算编制、审批、执行监控、调整 | 数据库设计已完成 |
| **期间结账** (period_closing) | 会计期间锁定与结账流程 | 数据库设计已完成 |
| **经营看板** (dashboard) | 首页经营数据概览 | 基础骨架已可用 |
| **认证鉴权** (auth) | 登录、会话、Token 管理 | 已实现 |

## 技术栈

| 层级 | 技术选型 |
|------|---------|
| 后端框架 | Spring Boot 3.x + JDK 21 |
| 构建工具 | Maven |
| 数据访问 | MyBatis-Plus |
| 工具库 | Lombok + Hutool |
| 前端框架 | Vue 3 + Vue Router |
| UI 组件库 | Element Plus |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 6.0 |
| 容器化 | Docker + Docker Compose |

## 快速开始

```bash
# 1. 启动基础服务（MySQL + Redis）
docker compose up -d profit-mysql profit-redis

# 2. 启动后端（在 backend/ 目录）
cd backend && mvn spring-boot:run

# 3. 启动前端（在 frontend/ 目录）
cd frontend && npm install && npm run dev
```

## 访问地址

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:3100 |
| 后端 API | http://localhost:8080 |

## 项目结构

```text
profit-decision-system/
├── backend/                # 后端 Spring Boot 工程
│   ├── src/main/java/com/profit/
│   │   ├── auth/           # 认证鉴权（登录/Token/拦截器）
│   │   ├── dashboard/      # 经营看板摘要
│   │   ├── controller/     # REST 接口层
│   │   ├── application/    # 用例编排层（事务/权限）
│   │   ├── domain/         # 领域层（实体/聚合/领域服务）
│   │   ├── infrastructure/ # 基础设施层（Mapper/Repository 实现）
│   │   ├── dto/            # 接口入参
│   │   ├── vo/             # 接口出参
│   │   ├── entity/         # 数据实体
│   │   ├── config/         # 配置类
│   │   └── common/         # 统一返回值/异常处理/工具
│   └── src/main/resources/ # 配置文件
├── frontend/               # 前端 Vue 3 工程
│   └── src/
│       ├── api/            # 后端接口调用
│       ├── views/          # 页面视图
│       ├── router/         # 路由配置
│       ├── components/     # 公共组件
│       ├── layouts/        # 页面布局
│       ├── stores/         # 状态管理
│       └── utils/          # 工具函数
├── database/               # 数据库脚本
├── docs/                   # 设计文档
│   ├── current-tech-plan.md            # 当前技术方案
│   ├── todo-writer.md                  # 活跃待办板
│   ├── account-subject-domain-design.md # 会计科目领域设计
│   ├── account-subject-page-dsl.md     # 会计科目页面 DSL
│   ├── requirements.md                 # 需求文档
│   ├── product-design.md               # 产品设计
│   ├── domain-model.md                 # DDD 领域模型总览
│   ├── database-design.md              # 数据库设计
│   └── development-standards.md        # 开发规范
└── docker-compose.yml      # 本地基础设施编排
```

## 当前进度

**已完成：**
- 后端基础工程骨架（Spring Boot + 认证 + 健康检查 + 仪表盘）
- 前端基础工程骨架（Vue 3 + 登录页 + 首页 + 布局）
- Docker 本地环境（MySQL + Redis）
- 领域模型总设计（Fact → Attribution → Metrics → Decision）
- 会计科目领域设计 + 页面 DSL
- 需求文档 + 产品设计文档

**进行中：**
- 会计科目模块的数据库建表 + 后端分层实现

**待启动：**
- 往来方模块（复用科目模式）
- 收支记录模块
- 前后端联调闭环

## 文档索引

| 文档 | 用途 |
|------|------|
| [当前技术方案](docs/current-tech-plan.md) | 技术选型、分层架构、实施路线 |
| [活跃待办板](docs/todo-writer.md) | 当前开发任务与优先级 |
| [后端架构](backend/ARCHITECTURE.md) | 后端分层结构与依赖 |
| [会计科目领域设计](docs/account-subject-domain-design.md) | account_subject 的 DDD 设计与 UML |
| [会计科目页面 DSL](docs/account-subject-page-dsl.md) | account_subject 页面结构与状态机 |
| [需求文档](docs/requirements.md) | 完整业务需求与验收标准 |
| [产品设计](docs/product-design.md) | 产品定位与核心对象模型 |
| [DDD 领域模型](docs/domain-model.md) | 全域领域划分与数据流 |
| [数据库设计](docs/database-design.md) | 业务表结构与关系 |
| [开发规范](docs/development-standards.md) | 编码约定与命名规范 |

## 设计原则

1. **单一事实源**：所有经营数据通过 FactEvent 写入，不允许绕过
2. **可解释**：每一笔成本/收入都能追溯归因规则
3. **可决策**：指标自动生成决策建议，而不仅仅是展示数据
4. **DDD 分层**：Controller → Application → Domain → Infrastructure，领域规则不外泄
5. **主数据先行**：会计科目、往来方等基础数据必须先于业务数据落地
