# 后端启动说明

## 推荐方式

### 方式一：本地直接启动

适合日常开发和调试。

```bash
# 在后端工程根目录执行
mvn spring-boot:run
```

### 方式二：先打包再启动

适合需要验证打包结果时使用。

```bash
# 在后端工程根目录执行
mvn clean package -DskipTests
java -jar target/*.jar
```

## 环境要求

- JDK 21
- Maven
- MySQL
- Redis

## 启动前检查

- 数据库连接是否正确。
- Redis 地址和端口是否正确。
- `application.yml` 或 `application-dev.yml` 的配置是否齐全。
- 端口是否被占用。

## 说明

- 当前项目不再以 jeecg 脚手架为主线。
- 如果旧文档里还出现 jeecg 的路径或镜像说明，那是历史内容，不适用于当前项目。
