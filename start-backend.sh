#!/bin/bash
# jeecg-boot 后端启动脚本

set -e

PROJECT_DIR="/vol3/1000/private/workProject/profit-decision-system/backend/jeecg-boot/jeecg-boot"
JAR_FILE="$PROJECT_DIR/jeecg-module-system/jeecg-system-start/target/jeecg-system-start-3.9.2.jar"

echo "=========================================="
echo "  jeecg-boot 后端启动脚本"
echo "=========================================="
echo ""

# 检查 JAR 文件是否存在
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ 错误：JAR 文件不存在"
    echo "   路径：$JAR_FILE"
    echo ""
    echo "请先编译项目："
    echo "   cd $PROJECT_DIR"
    echo "   mvn clean package -DskipTests"
    exit 1
fi

echo "✅ 找到 JAR 文件：$JAR_FILE"
echo ""

# 检查 MySQL 和 Redis 是否运行
echo "检查依赖服务..."
if ! docker ps | grep -q profit-mysql; then
    echo "❌ MySQL 容器未运行"
    echo "   启动命令：docker compose up -d profit-mysql"
    exit 1
fi
echo "✅ MySQL 运行中"

if ! docker ps | grep -q profit-redis; then
    echo "❌ Redis 容器未运行"
    echo "   启动命令：docker compose up -d profit-redis"
    exit 1
fi
echo "✅ Redis 运行中"

echo ""
echo "=========================================="
echo "  启动 jeecg-boot 后端..."
echo "=========================================="
echo ""
echo "访问地址："
echo "  - 后端 API：http://localhost:8081/jeecg-boot/"
echo "  - 接口文档：http://localhost:8081/jeecg-boot/doc.html"
echo "  - 代码生成器：登录后进入 系统管理 → 开发工具 → 代码生成器"
echo ""
echo "登录信息："
echo "  - 用户名：admin"
echo "  - 密码：123456"
echo ""
echo "按 Ctrl+C 停止服务"
echo ""
echo "=========================================="
echo ""

# 使用 Docker 运行 JAR（避免安装 Java）
docker run --rm \
  --name jeecg-boot-app \
  --network host \
  -v "$JAR_FILE":/app/app.jar \
  -e SERVER_PORT="8081" \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/jeecg-boot?characterEncoding=UTF-8&useUnicode=true&useSSL=false&serverTimezone=Asia/Shanghai" \
  -e SPRING_DATASOURCE_USERNAME="root" \
  -e SPRING_DATASOURCE_PASSWORD="***" \
  -e SPRING_REDIS_HOST="localhost" \
  -e SPRING_REDIS_PORT="6380" \
  eclipse-temurin:17-jre-jammy \
  java -jar /app/app.jar
