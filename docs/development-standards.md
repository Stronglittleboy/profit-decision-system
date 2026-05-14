# 飞牛经营系统 - 开发规范

## 1. 技术栈

### 后端

- Spring Boot
- JDK 21
- Maven
- Spring Web
- MyBatis-Plus
- MySQL
- Redis
- Lombok
- Hutool

### 前端

- Vue 3
- Vue Router
- Element Plus

## 2. 项目结构

### 后端

```text
backend/src/main/java/com/xxx/
├── controller/
├── service/
├── service/impl/
├── mapper/
├── entity/
├── dto/
├── vo/
├── config/
└── common/
```

### 前端

```text
frontend/src/
├── api/
├── views/
├── router/
├── components/
├── layouts/
└── utils/
```

## 3. 命名规范

- Java 类名使用大驼峰。
- 方法和变量使用小驼峰。
- 数据库表和字段使用小写下划线。
- 前端组件名使用大驼峰。

## 4. 编码约定

- Controller 只做参数接收、校验和响应返回。
- Service 负责业务规则，不拼接前端细节。
- Mapper 负责数据访问，复杂 SQL 优先写 XML。
- 接口入参统一使用 DTO，返回结果统一使用 VO。
- 公共工具、常量、异常码放到 `common` 目录。

## 5. 开发流程

1. 先定义接口和数据结构。
2. 再实现后端 Service 和 Mapper。
3. 再补前端页面、路由和 API 调用。
4. 最后补单元测试和联调验证。

## 6. 前端约定

- 页面按业务模块拆分。
- 路由保持清晰的一级模块和二级页面。
- 表单、弹窗、列表尽量复用组件。
- UI 统一使用 Element Plus。

## 7. 后端约定

- 实体类与数据库表字段一一对应。
- 统一返回结构，避免每个接口各写一套格式。
- 常用工具优先使用 Lombok 和 Hutool，减少重复代码。
- MyBatis-Plus 适合常规 CRUD，复杂逻辑再补充自定义实现。
