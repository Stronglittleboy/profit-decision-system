#!/bin/bash
# 飞牛经营系统前端启动脚本

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FRONTEND_DIR="$SCRIPT_DIR/frontend"

echo "=========================================="
echo "  飞牛经营系统前端启动脚本"
echo "=========================================="
echo ""

if [ ! -d "$FRONTEND_DIR" ]; then
    echo "❌ 错误：未找到前端目录"
    echo "   目录：$FRONTEND_DIR"
    exit 1
fi

if [ ! -f "$FRONTEND_DIR/package.json" ]; then
    echo "❌ 错误：未找到前端项目"
    echo "   目录：$FRONTEND_DIR"
    echo "   请确认前端工程已放入 frontend 目录"
    exit 1
fi

if ! command -v pnpm >/dev/null 2>&1; then
    echo "❌ 错误：未找到 pnpm"
    echo "   安装命令：npm install -g pnpm"
    exit 1
fi

echo "✅ 找到前端工程：$FRONTEND_DIR"
echo ""
echo "访问地址："
echo "  - 前端页面：http://localhost:3100"
echo ""
echo "技术栈：Vue 3 + Vue Router + Element Plus"
echo ""
echo "按 Ctrl+C 停止服务"
echo ""
echo "=========================================="
echo ""

cd "$FRONTEND_DIR"

if [ ! -d node_modules ]; then
    pnpm install
fi

pnpm dev
