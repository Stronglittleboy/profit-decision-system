# 飞牛经营系统 - 快速启动指南

## 📋 前置条件

✅ Docker 已安装并运行
✅ Node.js v22.22.0 已安装
✅ pnpm 已安装

---

## 🚀 一键启动（推荐）

### 1. 启动数据库服务
```bash
cd /vol3/1000/private/workProject/profit-decision-system
docker compose up -d profit-mysql profit-redis
```

### 2. 启动后端
```bash
./start-backend.sh
```

### 3. 启动前端（新终端）
```bash
./start-frontend.sh
```

---

## 📝 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端页面 | http://localhost:3100 | Vue3 前端 |
| 后端 API | http://localhost:8081/jeecg-boot | Spring Boot 后端 |
| 接口文档 | http://localhost:8081/jeecg-boot/doc.html | Knife4j 文档 |
| 代码生成器 | 登录后进入 系统管理 → 开发工具 | 在线代码生成 |

---

## 🔑 登录信息

- **用户名**：admin
- **密码**：123456

---

## 📂 项目结构

```
/vol3/1000/private/workProject/profit-decision-system/
├── backend/
│   └── jeecg-boot/jeecg-boot/          # 后端源码
│       ├── jeecg-boot-module/
│       │   └── jeecg-boot-module-profit/  # profit 模块
│       └── jeecg-module-system/
│           └── jeecg-system-start/        # 启动类
├── frontend/
│   └── jeecgboot-vue3/                 # 前端源码
├── database/
│   ├── schema-v3-final.sql             # 业务表结构
│   └── dict-data.sql                   # 字典数据
├── docs/                               # 文档目录
├── start-backend.sh                    # 后端启动脚本
├── start-frontend.sh                   # 前端启动脚本
└── docker-compose.yml                  # Docker 编排
```

---

## 🛠️ 开发流程

### 第一步：导入字典数据（首次启动）

```bash
# 连接到 MySQL
docker exec -it profit-mysql mysql -uroot -p123456 jeecg-boot

# 执行字典数据脚本
source /vol3/1000/private/workProject/profit-decision-system/database/dict-data.sql
```

### 第二步：使用代码生成器

1. 登录系统（admin/123456）
2. 进入 **系统管理 → 开发工具 → 代码生成器**
3. 点击 **同步数据库**
4. 选择要生成的表（如 `profit_counterparty`）
5. 配置生成参数（参考 `docs/code-generator-guide.md`）
6. 点击 **生成代码**
7. 下载并解压到对应目录

### 第三步：重启服务

```bash
# 重启后端（Ctrl+C 停止，然后重新运行）
./start-backend.sh

# 重启前端（Ctrl+C 停止，然后重新运行）
./start-frontend.sh
```

---

## 📚 文档索引

| 文档 | 路径 | 说明 |
|------|------|------|
| 代码生成器指南 | `docs/code-generator-guide.md` | 详细的代码生成步骤 |
| 数据库设计 | `docs/database-design.md` | 数据库表结构说明 |
| API 接口文档 | `docs/api-documentation.md` | 接口定义 |
| 前端页面清单 | `docs/frontend-pages-list.md` | 29个页面清单 |
| 开发规范 | `docs/development-standards.md` | 代码规范 |
| 工作流程规范 | `docs/workflow-standards.md` | 开发流程 |

---

## 🐛 常见问题

### Q1: 后端启动失败，提示 "JAR 文件不存在"
**A**: 需要先编译项目：
```bash
cd /vol3/1000/private/workProject/profit-decision-system/backend/jeecg-boot/jeecg-boot
docker run --rm -v "$(pwd)":/app -w /app maven:3.8.6-eclipse-temurin-17 mvn clean package -DskipTests
```

### Q2: 前端启动失败，提示 "依赖未安装"
**A**: 需要先安装依赖：
```bash
cd /vol3/1000/private/workProject/profit-decision-system/frontend/jeecgboot-vue3
pnpm install
```

### Q3: 数据库连接失败
**A**: 检查 MySQL 容器是否运行：
```bash
docker ps | grep profit-mysql
# 如果未运行，启动它：
docker compose up -d profit-mysql
```

### Q4: Redis 连接失败
**A**: 检查 Redis 容器是否运行：
```bash
docker ps | grep profit-redis
# 如果未运行，启动它：
docker compose up -d profit-redis
```

### Q5: 前端页面空白
**A**: 检查：
1. 后端是否启动（http://localhost:8080/jeecg-boot）
2. 浏览器控制台是否有报错
3. 清除浏览器缓存

---

## 🔧 手动启动（不使用脚本）

### 启动后端
```bash
cd /vol3/1000/private/workProject/profit-decision-system/backend/jeecg-boot/jeecg-boot

# 使用 Docker 运行 JAR
docker run --rm \
  --name jeecg-boot-app \
  --network profit-decision-system_profit-network \
  -p 8080:8080 \
  -v "$(pwd)/jeecg-module-system/jeecg-system-start/target/jeecg-system-start-3.9.2.jar":/app/app.jar \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://profit-mysql:3306/jeecg-boot?characterEncoding=UTF-8&useUnicode=true&useSSL=false&serverTimezone=Asia/Shanghai" \
  -e SPRING_DATASOURCE_USERNAME="root" \
  -e SPRING_DATASOURCE_PASSWORD="123456" \
  -e SPRING_REDIS_HOST="profit-redis" \
  -e SPRING_REDIS_PORT="6379" \
  eclipse-temurin:17-jre-jammy \
  java -jar /app/app.jar
```

### 启动前端
```bash
cd /vol3/1000/private/workProject/profit-decision-system/frontend/jeecgboot-vue3
pnpm dev
```

---

## 📊 开发进度

- ✅ Day 1-4: 设计阶段（100%）
- 🔄 Day 5: 开发阶段（进行中）
  - ✅ 环境配置
  - ✅ 前端依赖安装
  - 🔄 后端编译
  - ⏳ 代码生成
  - ⏳ 核心业务开发

---

## 🎯 下一步计划

1. ⏳ 等待后端编译完成
2. ⏳ 启动后端服务
3. ⏳ 导入字典数据
4. ⏳ 生成第一批模块（客户/供应商/会计科目/项目/合同）
5. ⏳ 手写核心业务逻辑（收支/应收应付/期间结账）

---

**预计今日完成：基础模块生成 + 部分核心逻辑**
