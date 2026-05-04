# Spring Boot 后端架构

## 技术栈

- Spring Boot 2.7+
- MyBatis-Plus
- MySQL 8.0
- Redis（缓存）
- RabbitMQ（异步归因）

---

## 模块划分（DDD 分层）

```
profit-system/
└── src/main/java/com/profit/
    ├── domain/                 # 领域层
    │   ├── fact/              # 事实域
    │   │   ├── FactEvent.java
    │   │   └── FactRepository.java
    │   ├── attribution/       # 归因域
    │   │   ├── Attribution.java
    │   │   ├── AttributionRule.java
    │   │   ├── AttributionService.java
    │   │   └── AttributionRepository.java
    │   ├── metrics/           # 指标域
    │   │   ├── MetricSnapshot.java
    │   │   ├── MetricService.java
    │   │   └── MetricRepository.java
    │   ├── decision/          # 决策域
    │   │   ├── DecisionView.java
    │   │   └── DecisionService.java
    │   └── org/               # 组织域
    │       ├── OrgUnit.java
    │       └── User.java
    ├── application/            # 应用层
    │   ├── FactApplicationService.java
    │   ├── MetricApplicationService.java
    │   └── DecisionApplicationService.java
    ├── infrastructure/         # 基础设施层
    │   ├── persistence/       # 持久化
    │   │   ├── FactMapper.java
    │   │   ├── AttributionMapper.java
    │   │   └── MetricMapper.java
    │   └── messaging/         # 消息队列
    │       └── AttributionEventListener.java
    └── interfaces/             # 接口层
        ├── rest/              # REST API
        │   ├── FactController.java
        │   ├── MetricController.java
        │   └── DecisionController.java
        └── dto/               # DTO
            ├── FactCreateDTO.java
            └── MetricQueryDTO.java
```

---

## 核心流程

### 1. 事实录入流程
```
Controller → ApplicationService → Domain → Repository
                ↓
            发布事件（异步归因）
```

### 2. 归因计算流程
```
EventListener → AttributionService → AttributionRepository
```

### 3. 指标计算流程
```
定时任务 → MetricService → 查询归因 → 计算指标 → 保存快照
```

### 4. 决策生成流程
```
Controller → DecisionService → 查询指标 + 目标 → 生成建议
```

---

## 配置文件结构

```
src/main/resources/
├── application.yml            # 主配置
├── application-dev.yml        # 开发环境
├── application-prod.yml       # 生产环境
└── mapper/                    # MyBatis XML
    ├── FactMapper.xml
    ├── AttributionMapper.xml
    └── MetricMapper.xml
```

---

## 依赖管理（pom.xml 关键依赖）

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>3.5.3</version>
    </dependency>
    
    <!-- MySQL -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
    
    <!-- Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    
    <!-- RabbitMQ -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-amqp</artifactId>
    </dependency>
</dependencies>
```
