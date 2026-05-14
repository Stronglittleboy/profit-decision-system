# 后端架构

## 技术栈

- Spring Boot 3.x
- JDK 21
- Maven
- Spring Web
- MyBatis-Plus
- MySQL
- Redis
- Lombok
- Hutool

## 分层结构

```text
src/main/java/com/profit/
├── controller/   # REST 接口
├── auth/         # 登录、会话、拦截器
├── dashboard/    # 首页摘要数据
├── application/  # 用例编排、事务边界
├── domain/        # 领域模型、聚合、领域服务
├── infrastructure/ # 持久化适配、外部依赖
├── service/      # 业务接口
│   └── impl/     # 业务实现
├── mapper/       # 数据访问接口
├── entity/       # 数据实体
├── dto/          # 入参对象
├── vo/           # 出参对象
├── config/       # 配置类
└── common/       # 通用工具、统一返回值、异常处理
```

## 设计原则

- Controller 只负责接收参数和返回结果。
- Application 负责业务编排和事务边界，不承载领域规则。
- Domain 负责聚合、实体、值对象、领域服务和业务规则。
- Infrastructure 负责 Mapper、数据库适配和外部依赖封装。
- Mapper 负责持久化访问，复杂查询可拆到 XML。
- DTO、VO、Domain Model 分离，避免接口模型和持久化模型混用。
- 公共返回值、异常码、工具方法统一放在 `common`。

## 关键依赖

```xml
<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
  </dependency>
  <dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
  </dependency>
  <dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
  </dependency>
  <dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-all</artifactId>
  </dependency>
</dependencies>
```

## 启动方式

```bash
mvn spring-boot:run
```

如果需要打包：

```bash
mvn clean package -DskipTests
```

## 数据库约定

- 本地开发使用 `docker-compose.yml` 启动 `profit-mysql`。
- 后端默认连接 Docker MySQL，不再使用内存数据替代真实模块。
- 领域模型与数据库表结构必须同步设计后再编码。
