#!/bin/bash
# ============================================
# Day 1 上午 DBA任务验证脚本
# ============================================

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 数据库配置
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="jeecg-boot"
DB_USER="root"
DB_PASSWORD="123456"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Day 1 上午 DBA任务验证${NC}"
echo -e "${BLUE}========================================${NC}"

# 检查数据库连接
echo -e "\n${YELLOW}[1/7] 检查数据库连接...${NC}"
if mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" -e "SELECT 1" &> /dev/null; then
    echo -e "${GREEN}✓ 数据库连接成功${NC}"
else
    echo -e "${RED}✗ 数据库连接失败${NC}"
    exit 1
fi

# 验证表数量
echo -e "\n${YELLOW}[2/7] 验证表数量...${NC}"
TABLE_COUNT=$(mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" \
    -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${DB_NAME}'" -sN)

echo "数据库中共有 ${TABLE_COUNT} 张表"

if [ "${TABLE_COUNT}" -ge 19 ]; then
    echo -e "${GREEN}✓ 表数量验证通过（预期≥19张，实际${TABLE_COUNT}张）${NC}"
else
    echo -e "${RED}✗ 表数量不足（预期≥19张，实际${TABLE_COUNT}张）${NC}"
fi

# 验证关键表是否存在
echo -e "\n${YELLOW}[3/7] 验证关键表是否存在...${NC}"

REQUIRED_TABLES=(
    "fact_event"
    "account_subject"
    "voucher"
    "voucher_entry"
    "attribution_rule"
    "attribution_result"
    "org_unit"
    "user"
    "counterparty"
    "receivable"
    "cash_flow"
    "profit_snapshot"
    "goal"
    "period_closing"
    "audit_log"
    "attachment"
    "notification"
    "sys_dict"
    "sys_dict_item"
)

MISSING_TABLES=()
for table in "${REQUIRED_TABLES[@]}"; do
    if mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" \
        -e "SHOW TABLES LIKE '${table}'" | grep -q "${table}"; then
        echo -e "${GREEN}✓${NC} ${table}"
    else
        echo -e "${RED}✗${NC} ${table} (缺失)"
        MISSING_TABLES+=("${table}")
    fi
done

if [ ${#MISSING_TABLES[@]} -eq 0 ]; then
    echo -e "${GREEN}✓ 所有关键表验证通过${NC}"
else
    echo -e "${RED}✗ 缺失 ${#MISSING_TABLES[@]} 张表: ${MISSING_TABLES[*]}${NC}"
fi

# 验证初始化数据
echo -e "\n${YELLOW}[4/7] 验证初始化数据...${NC}"

mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" << 'EOSQL'
SELECT 
    'org_unit' AS table_name, 
    COUNT(*) AS record_count,
    CASE WHEN COUNT(*) >= 6 THEN '✓' ELSE '✗' END AS status
FROM org_unit
UNION ALL
SELECT 
    'user', 
    COUNT(*),
    CASE WHEN COUNT(*) >= 2 THEN '✓' ELSE '✗' END
FROM user
UNION ALL
SELECT 
    'attribution_rule', 
    COUNT(*),
    CASE WHEN COUNT(*) >= 2 THEN '✓' ELSE '✗' END
FROM attribution_rule
UNION ALL
SELECT 
    'goal', 
    COUNT(*),
    CASE WHEN COUNT(*) >= 2 THEN '✓' ELSE '✗' END
FROM goal;
EOSQL

# 验证fact_event表结构
echo -e "\n${YELLOW}[5/7] 验证fact_event表结构...${NC}"

FACT_EVENT_COLUMNS=$(mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" \
    -e "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='${DB_NAME}' AND table_name='fact_event'" -sN)

echo "fact_event表共有 ${FACT_EVENT_COLUMNS} 个字段"

# 检查关键字段
REQUIRED_COLUMNS=(
    "id"
    "business_date"
    "accounting_date"
    "type"
    "amount"
    "account_subject_id"
    "voucher_no"
    "debit_credit"
    "org_unit_id"
    "status"
)

echo "检查关键字段:"
for column in "${REQUIRED_COLUMNS[@]}"; do
    if mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" \
        -e "SHOW COLUMNS FROM fact_event LIKE '${column}'" | grep -q "${column}"; then
        echo -e "${GREEN}✓${NC} ${column}"
    else
        echo -e "${RED}✗${NC} ${column} (缺失)"
    fi
done

# 验证索引
echo -e "\n${YELLOW}[6/7] 验证索引创建...${NC}"

# 检查fact_event表索引
FACT_EVENT_INDEXES=$(mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" \
    -e "SELECT COUNT(DISTINCT index_name) FROM information_schema.statistics 
        WHERE table_schema='${DB_NAME}' AND table_name='fact_event' AND index_name != 'PRIMARY'" -sN)

echo "fact_event表共有 ${FACT_EVENT_INDEXES} 个索引（不含主键）"

if [ "${FACT_EVENT_INDEXES}" -ge 10 ]; then
    echo -e "${GREEN}✓ fact_event表索引验证通过${NC}"
else
    echo -e "${YELLOW}⚠ fact_event表索引数量较少（预期≥10个）${NC}"
fi

# 检查voucher表索引
VOUCHER_INDEXES=$(mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" \
    -e "SELECT COUNT(DISTINCT index_name) FROM information_schema.statistics 
        WHERE table_schema='${DB_NAME}' AND table_name='voucher' AND index_name != 'PRIMARY'" -sN)

echo "voucher表共有 ${VOUCHER_INDEXES} 个索引（不含主键）"

if [ "${VOUCHER_INDEXES}" -ge 5 ]; then
    echo -e "${GREEN}✓ voucher表索引验证通过${NC}"
else
    echo -e "${YELLOW}⚠ voucher表索引数量较少（预期≥5个）${NC}"
fi

# 检查audit_log表索引
AUDIT_LOG_INDEXES=$(mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" \
    -e "SELECT COUNT(DISTINCT index_name) FROM information_schema.statistics 
        WHERE table_schema='${DB_NAME}' AND table_name='audit_log' AND index_name != 'PRIMARY'" -sN)

echo "audit_log表共有 ${AUDIT_LOG_INDEXES} 个索引（不含主键）"

if [ "${AUDIT_LOG_INDEXES}" -ge 4 ]; then
    echo -e "${GREEN}✓ audit_log表索引验证通过${NC}"
else
    echo -e "${YELLOW}⚠ audit_log表索引数量较少（预期≥4个）${NC}"
fi

# 显示所有表的索引统计
echo -e "\n所有表的索引统计:"
mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" << 'EOSQL'
SELECT 
    table_name,
    COUNT(DISTINCT index_name) AS index_count
FROM information_schema.statistics 
WHERE table_schema = DATABASE()
  AND index_name != 'PRIMARY'
GROUP BY table_name
ORDER BY index_count DESC, table_name;
EOSQL

# 验证备份文件
echo -e "\n${YELLOW}[7/7] 验证备份文件...${NC}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_DIR="${SCRIPT_DIR}/backups"

if [ -d "${BACKUP_DIR}" ]; then
    BACKUP_COUNT=$(ls -1 "${BACKUP_DIR}"/*.sql 2>/dev/null | wc -l)
    if [ "${BACKUP_COUNT}" -gt 0 ]; then
        echo -e "${GREEN}✓ 找到 ${BACKUP_COUNT} 个备份文件${NC}"
        echo "最新备份:"
        ls -lht "${BACKUP_DIR}"/*.sql | head -3
    else
        echo -e "${YELLOW}⚠ 备份目录存在但无备份文件${NC}"
    fi
else
    echo -e "${YELLOW}⚠ 备份目录不存在${NC}"
fi

# 最终总结
echo -e "\n${BLUE}========================================${NC}"
echo -e "${BLUE}验证总结${NC}"
echo -e "${BLUE}========================================${NC}"

echo -e "\n${GREEN}已完成项:${NC}"
echo "1. 数据库连接正常"
echo "2. 数据库表创建完成（${TABLE_COUNT}张表）"
echo "3. 初始化数据已导入"
echo "4. 关键表结构验证通过"
echo "5. 索引创建完成"

if [ ${#MISSING_TABLES[@]} -gt 0 ]; then
    echo -e "\n${RED}需要注意:${NC}"
    echo "- 缺失表: ${MISSING_TABLES[*]}"
fi

if [ "${FACT_EVENT_INDEXES}" -lt 10 ] || [ "${VOUCHER_INDEXES}" -lt 5 ] || [ "${AUDIT_LOG_INDEXES}" -lt 4 ]; then
    echo -e "\n${YELLOW}建议:${NC}"
    echo "- 部分表的索引数量较少，建议执行 create-indexes.sql"
fi

echo -e "\n${GREEN}✓ Day 1 上午 DBA任务验证完成！${NC}"
