#!/bin/bash
# jeecg-boot 前端启动脚本

set -e

PROJECT_DIR="/vol3/1000/private/workProject/profit-decision-system/frontend/jeecgboot-vue3"
PNPM="/home/admin/.npm-global/bin/pnpm"

echo "=========================================="
echo "  jeecg-boot 前端启动脚本"
echo "=========================================="
echo ""

# 检查 pnpm 是否安装
if [ ! -f "$PNPM" ]; then
    echo "❌ 错误：pnpm 未安装"
    echo "   安装命令：npm install -g pnpm"
    exit 1
fi
echo "✅ pnpm 已安装"

# 检查 node_modules 是否存在
if [ ! -d "$PROJECT_DIR/node_modules" ]; then
    echo "❌ 错误：依赖未安装"
    echo "   安装命令：cd $PROJECT_DIR && pnpm install"
    exit 1
fi
echo "✅ 依赖已安装"

echo ""
echo "=========================================="
echo "  启动前端开发服务器..."
echo "=========================================="
echo ""
echo "访问地址："
echo "  - 前端页面：http://localhost:3100"
echo ""
echo "后端 API 代理："
echo "  - 代理到：http://localhost:8080/jeecg-boot"
echo ""
echo "登录信息："
echo "  - 用户名：admin"
echo "  - 密码：123456"
echo ""
echo "按 Ctrl+C 停止服务"
echo ""
echo "=========================================="
echo ""

# 启动前端
cd "$PROJECT_DIR"
$PNPM dev
