#!/bin/bash
# ============================================
# 数据库连接测试脚本
# ============================================

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}数据库连接诊断${NC}"
echo -e "${BLUE}========================================${NC}"

# 数据库配置
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="jeecg-boot"
DB_USER="root"
DB_PASSWORD="123456"

# 1. 检查Docker是否安装
echo -e "\n${YELLOW}[1/6] 检查Docker...${NC}"
if command -v docker &> /dev/null; then
    echo -e "${GREEN}✓ Docker已安装: $(docker --version)${NC}"
else
    echo -e "${RED}✗ Docker未安装${NC}"
    echo "请安装Docker: https://docs.docker.com/engine/install/"
    exit 1
fi

# 2. 检查MySQL容器状态
echo -e "\n${YELLOW}[2/6] 检查MySQL容器状态...${NC}"
CONTAINER_STATUS=$(docker ps -a --filter "name=profit-mysql" --format "{{.Status}}" 2>/dev/null || echo "not found")

if [[ "$CONTAINER_STATUS" == "not found" ]]; then
    echo -e "${RED}✗ MySQL容器不存在${NC}"
    echo "请启动容器: cd /home/hlw/work/code/profit-decision-system && docker compose up -d profit-mysql"
    exit 1
elif [[ "$CONTAINER_STATUS" == Up* ]]; then
    echo -e "${GREEN}✓ MySQL容器正在运行${NC}"
    echo "  状态: $CONTAINER_STATUS"
else
    echo -e "${RED}✗ MySQL容器未运行${NC}"
    echo "  状态: $CONTAINER_STATUS"
    echo "请启动容器: docker start profit-mysql"
    exit 1
fi

# 3. 检查端口监听
echo -e "\n${YELLOW}[3/6] 检查端口监听...${NC}"
if nc -zv localhost 3306 2>&1 | grep -q "succeeded"; then
    echo -e "${GREEN}✓ 端口3306正在监听${NC}"
else
    echo -e "${RED}✗ 端口3306未监听${NC}"
    echo "请检查容器日志: docker logs profit-mysql"
    exit 1
fi

# 4. 检查MySQL客户端
echo -e "\n${YELLOW}[4/6] 检查MySQL客户端...${NC}"
if command -v mysql &> /dev/null; then
    echo -e "${GREEN}✓ MySQL客户端已安装: $(mysql --version)${NC}"
else
    echo -e "${RED}✗ MySQL客户端未安装${NC}"
    echo "请安装: sudo apt-get install mysql-client"
    exit 1
fi

# 5. 测试数据库连接
echo -e "\n${YELLOW}[5/6] 测试数据库连接...${NC}"
if mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" -e "SELECT 1" &> /dev/null; then
    echo -e "${GREEN}✓ 数据库连接成功${NC}"
else
    echo -e "${RED}✗ 数据库连接失败${NC}"
    echo "请检查:"
    echo "  1. 容器是否完全启动（等待30秒）"
    echo "  2. 密码是否正确（默认: 123456）"
    echo "  3. 查看容器日志: docker logs profit-mysql"
    exit 1
fi

# 6. 检查数据库版本和状态
echo -e "\n${YELLOW}[6/6] 检查数据库版本和状态...${NC}"
mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" << 'EOSQL'
SELECT 
    VERSION() AS mysql_version,
    @@character_set_database AS charset,
    @@collation_database AS collation;

SELECT 
    SCHEMA_NAME AS database_name,
    DEFAULT_CHARACTER_SET_NAME AS charset,
    DEFAULT_COLLATION_NAME AS collation
FROM information_schema.SCHEMATA 
WHERE SCHEMA_NAME = 'jeecg-boot';
EOSQL

# 检查表数量
TABLE_COUNT=$(mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" \
    -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${DB_NAME}'" -sN 2>/dev/null || echo "0")

echo -e "\n当前数据库表数量: ${TABLE_COUNT}"

# 总结
echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}诊断完成${NC}"
echo -e "${GREEN}========================================${NC}"

if [ "${TABLE_COUNT}" -ge 19 ]; then
    echo -e "\n${GREEN}✓ 数据库已就绪，可以开始使用${NC}"
    echo "表数量: ${TABLE_COUNT}"
else
    echo -e "\n${YELLOW}⚠ 数据库连接正常，但表未创建或不完整${NC}"
    echo "当前表数量: ${TABLE_COUNT}"
    echo "请执行: ./day1-dba-tasks.sh"
fi

echo -e "\n下一步:"
if [ "${TABLE_COUNT}" -lt 19 ]; then
    echo "1. 执行DBA任务: ./day1-dba-tasks.sh"
    echo "2. 验证结果: ./verify-dba-tasks.sh"
else
    echo "1. 验证数据库: ./verify-dba-tasks.sh"
    echo "2. 启动后端服务"
    echo "3. 运行测试"
fi
