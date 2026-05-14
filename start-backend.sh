#!/bin/bash
# 飞牛经营系统后端启动脚本

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR/backend"

echo "=========================================="
echo "  飞牛经营系统后端启动脚本"
echo "=========================================="
echo ""

if [ ! -d "$BACKEND_DIR" ]; then
    echo "❌ 错误：未找到后端目录"
    echo "   目录：$BACKEND_DIR"
    exit 1
fi

if [ ! -f "$BACKEND_DIR/pom.xml" ]; then
    echo "❌ 错误：未找到后端 Maven 项目"
    echo "   目录：$BACKEND_DIR"
    echo "   请确认后端工程已放入 backend 目录"
    exit 1
fi

if ! command -v mvn >/dev/null 2>&1; then
    echo "❌ 错误：未找到 Maven"
    exit 1
fi

if ! command -v java >/dev/null 2>&1; then
    echo "❌ 错误：未找到 Java"
    exit 1
fi

echo "✅ 找到后端工程：$BACKEND_DIR"
echo ""
echo "访问地址："
echo "  - 后端 API：http://localhost:8080"
echo "  - 接口文档：按项目实际 OpenAPI/Swagger 配置访问"
echo ""
echo "技术栈：Spring Boot + JDK 21 + Maven + MyBatis-Plus + Lombok + Hutool"
echo ""
echo "按 Ctrl+C 停止服务"
echo ""
echo "=========================================="
echo ""

cd "$BACKEND_DIR"
mvn spring-boot:run
